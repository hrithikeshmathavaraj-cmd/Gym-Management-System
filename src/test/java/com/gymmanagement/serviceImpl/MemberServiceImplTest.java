package com.gymmanagement.serviceImpl;

import com.gymmanagement.dto.MemberRequest;
import com.gymmanagement.entity.Member;
import com.gymmanagement.entity.User;
import com.gymmanagement.mapper.MemberMapper;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.repository.PlanRepository;
import com.gymmanagement.repository.UserRepository;
import com.gymmanagement.service.realtime.RealtimeDashboardPublisher;
import com.gymmanagement.util.MemberCodeGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberServiceImplTest {

    @Test
    void resetPasswordUpdatesLinkedMemberUserAccount() {
        MemberRepository memberRepository = mock(MemberRepository.class);
        PlanRepository planRepository = mock(PlanRepository.class);
        MemberMapper memberMapper = mock(MemberMapper.class);
        MemberCodeGenerator memberCodeGenerator = mock(MemberCodeGenerator.class);
        RealtimeDashboardPublisher realtimeDashboardPublisher = mock(RealtimeDashboardPublisher.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        MemberServiceImpl service = new MemberServiceImpl(
                memberRepository,
                planRepository,
                memberMapper,
                memberCodeGenerator,
                realtimeDashboardPublisher,
                userRepository,
                passwordEncoder
        );

        Member member = new Member();
        member.setId("member-1");
        member.setMemberCode("M-100");

        User user = User.builder().username("M-100").password("old-hash").build();

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(userRepository.findByUsername("M-100")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass123")).thenReturn("encoded-new-password");

        service.resetPassword("member-1", "NewPass123");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encoded-new-password", captor.getValue().getPassword());
    }
}
