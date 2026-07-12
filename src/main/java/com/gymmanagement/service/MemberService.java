package com.gymmanagement.service;

import com.gymmanagement.dto.MemberRequest;
import com.gymmanagement.dto.MemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {

    MemberResponse createMember(MemberRequest request);

    MemberResponse updateMember(String id, MemberRequest request);

    void deleteMember(String id);

    MemberResponse getMemberById(String id);

    MemberResponse getMemberByCode(String memberCode);

    Page<MemberResponse> getAllMembers(Pageable pageable);

    Page<MemberResponse> searchMembers(String keyword, Pageable pageable);

    MemberResponse renewMembership(String id, String planId);

    MemberResponse setMemberLocked(String id, boolean locked);

    void resetPassword(String id, String newPassword);
}
