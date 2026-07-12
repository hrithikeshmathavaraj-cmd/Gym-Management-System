package com.gymmanagement.serviceImpl;

import com.gymmanagement.dto.MemberRequest;
import com.gymmanagement.dto.MemberResponse;
import com.gymmanagement.entity.Member;
import com.gymmanagement.entity.MembershipStatus;
import com.gymmanagement.entity.Plan;
import com.gymmanagement.entity.Role;
import com.gymmanagement.entity.User;
import com.gymmanagement.exception.DuplicateResourceException;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.mapper.MemberMapper;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.repository.PlanRepository;
import com.gymmanagement.repository.UserRepository;
import com.gymmanagement.service.MemberService;
import com.gymmanagement.service.realtime.RealtimeDashboardPublisher;
import com.gymmanagement.util.MemberCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Business logic for member lifecycle management: onboarding, updates,
 * search/pagination, and membership renewal. Creating a member also
 * auto-provisions a login account: the generated memberCode becomes their Login
 * ID, paired with the configured default member password, so they can sign in
 * immediately — no email address involved.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;
    private final MemberMapper memberMapper;
    private final MemberCodeGenerator memberCodeGenerator;
    private final RealtimeDashboardPublisher realtimeDashboardPublisher;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-member-password}")
    private String defaultMemberPassword;

    @Override
    @Transactional
    public MemberResponse createMember(MemberRequest request) {
        if (request.getEmail() != null && memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A member with this email already exists");
        }

        Plan plan = planRepository.findById(request.getMembershipPlan())
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found: " + request.getMembershipPlan()));

        Member member = memberMapper.toEntity(request);
        member.setMemberCode(memberCodeGenerator.generate());

        LocalDate joinDate = request.getJoinDate() != null ? request.getJoinDate() : LocalDate.now();
        member.setJoinDate(joinDate);
        member.setExpiryDate(joinDate.plusDays(plan.getDuration()));
        member.setStatus(MembershipStatus.ACTIVE);

        Member saved = memberRepository.save(member);

        // Auto-provision a login account for this member using their code as the Login ID.
        User loginAccount = User.builder()
                .name(saved.getName())
                .username(saved.getMemberCode())
                .password(passwordEncoder.encode(defaultMemberPassword))
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        userRepository.save(loginAccount);

        log.info("New member created: {} (Login ID: {})", saved.getName(), saved.getMemberCode());
        realtimeDashboardPublisher.publish("MEMBER_CREATED");

        return enrichWithPlanName(memberMapper.toResponse(saved), plan);
    }

    @Override
    @Transactional
    public MemberResponse updateMember(String id, MemberRequest request) {
        Member existing = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        memberMapper.updateEntityFromRequest(request, existing);

        Plan plan = planRepository.findById(existing.getMembershipPlan())
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found"));

        Member saved = memberRepository.save(existing);
        log.info("Member updated: {}", saved.getId());
        realtimeDashboardPublisher.publish("MEMBER_UPDATED");

        userRepository.findByUsername(saved.getMemberCode()).ifPresent(user -> {
            user.setName(saved.getName());
            userRepository.save(user);
        });

        return enrichWithPlanName(memberMapper.toResponse(saved), plan);
    }

    @Override
    @Transactional
    public void deleteMember(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        memberRepository.deleteById(id);
        userRepository.findByUsername(member.getMemberCode()).ifPresent(userRepository::delete);
        log.info("Member deleted: {}", id);
        realtimeDashboardPublisher.publish("MEMBER_DELETED");
    }

    @Override
    public MemberResponse getMemberById(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        Plan plan = planRepository.findById(member.getMembershipPlan()).orElse(null);
        return enrichWithPlanName(memberMapper.toResponse(member), plan);
    }

    @Override
    public MemberResponse getMemberByCode(String memberCode) {
        Member member = memberRepository.findByMemberCode(memberCode)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with code: " + memberCode));
        Plan plan = planRepository.findById(member.getMembershipPlan()).orElse(null);
        return enrichWithPlanName(memberMapper.toResponse(member), plan);
    }

    @Override
    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable).map(this::mapWithPlan);
    }

    @Override
    public Page<MemberResponse> searchMembers(String keyword, Pageable pageable) {
        return memberRepository
                .findByNameContainingIgnoreCaseOrMemberCodeContainingIgnoreCaseOrPhoneContaining(
                        keyword, keyword, keyword, pageable)
                .map(this::mapWithPlan);
    }

    @Override
    @Transactional
    public MemberResponse renewMembership(String id, String planId) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + planId));

        LocalDate baseDate = member.getExpiryDate() != null && member.getExpiryDate().isAfter(LocalDate.now())
                ? member.getExpiryDate()
                : LocalDate.now();

        member.setMembershipPlan(planId);
        member.setExpiryDate(baseDate.plusDays(plan.getDuration()));
        member.setStatus(MembershipStatus.ACTIVE);

        Member saved = memberRepository.save(member);

        // Renewing should also lift any auto-lock that was applied when the
        // membership expired, so the member can log in again immediately.
        userRepository.findByUsernameIgnoreCase(saved.getMemberCode()).ifPresent(user -> {
            if (!user.isEnabled()) {
                user.setEnabled(true);
                userRepository.save(user);
                log.info("Login re-enabled for member {} after renewal", saved.getMemberCode());
            }
        });

        log.info("Membership renewed for member: {}", saved.getId());
        realtimeDashboardPublisher.publish("MEMBER_RENEWED");

        return enrichWithPlanName(memberMapper.toResponse(saved), plan);
    }

    @Override
    @Transactional
    public MemberResponse setMemberLocked(String id, boolean locked) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        member.setStatus(locked ? MembershipStatus.SUSPENDED : MembershipStatus.ACTIVE);
        Member saved = memberRepository.save(member);

        userRepository.findByUsernameIgnoreCase(saved.getMemberCode()).ifPresent(user -> {
            user.setEnabled(!locked);
            userRepository.save(user);
        });

        log.info("Member {} {} by admin", saved.getMemberCode(), locked ? "locked" : "unlocked");
        realtimeDashboardPublisher.publish(locked ? "MEMBER_LOCKED" : "MEMBER_UNLOCKED");

        Plan plan = saved.getMembershipPlan() != null ? planRepository.findById(saved.getMembershipPlan()).orElse(null) : null;
        return enrichWithPlanName(memberMapper.toResponse(saved), plan);
    }

    @Override
    @Transactional
    public void resetPassword(String id, String newPassword) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        User user = userRepository.findByUsername(member.getMemberCode())
                .orElseThrow(() -> new ResourceNotFoundException("No login account found for this member"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset for member: {}", member.getMemberCode());
    }

    private MemberResponse mapWithPlan(Member member) {
        Plan plan = planRepository.findById(member.getMembershipPlan()).orElse(null);
        return enrichWithPlanName(memberMapper.toResponse(member), plan);
    }

    private MemberResponse enrichWithPlanName(MemberResponse response, Plan plan) {
        response.setMembershipPlanName(plan != null ? plan.getPlanName() : "Unknown");
        return response;
    }
}
