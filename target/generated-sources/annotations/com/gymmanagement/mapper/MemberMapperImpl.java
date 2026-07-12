package com.gymmanagement.mapper;

import com.gymmanagement.dto.MemberRequest;
import com.gymmanagement.dto.MemberResponse;
import com.gymmanagement.entity.Member;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-12T18:53:40+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class MemberMapperImpl implements MemberMapper {

    @Override
    public Member toEntity(MemberRequest request) {
        if ( request == null ) {
            return null;
        }

        Member.MemberBuilder member = Member.builder();

        member.name( request.getName() );
        member.age( request.getAge() );
        member.gender( request.getGender() );
        member.phone( request.getPhone() );
        member.email( request.getEmail() );
        member.address( request.getAddress() );
        member.membershipPlan( request.getMembershipPlan() );
        member.joinDate( request.getJoinDate() );

        return member.build();
    }

    @Override
    public MemberResponse toResponse(Member member) {
        if ( member == null ) {
            return null;
        }

        MemberResponse.MemberResponseBuilder memberResponse = MemberResponse.builder();

        memberResponse.id( member.getId() );
        memberResponse.memberCode( member.getMemberCode() );
        memberResponse.name( member.getName() );
        memberResponse.age( member.getAge() );
        memberResponse.gender( member.getGender() );
        memberResponse.phone( member.getPhone() );
        memberResponse.email( member.getEmail() );
        memberResponse.address( member.getAddress() );
        memberResponse.membershipPlan( member.getMembershipPlan() );
        memberResponse.joinDate( member.getJoinDate() );
        memberResponse.expiryDate( member.getExpiryDate() );
        memberResponse.status( member.getStatus() );
        memberResponse.profileImage( member.getProfileImage() );
        memberResponse.createdAt( member.getCreatedAt() );
        memberResponse.updatedAt( member.getUpdatedAt() );

        return memberResponse.build();
    }

    @Override
    public void updateEntityFromRequest(MemberRequest request, Member member) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            member.setName( request.getName() );
        }
        member.setAge( request.getAge() );
        if ( request.getGender() != null ) {
            member.setGender( request.getGender() );
        }
        if ( request.getPhone() != null ) {
            member.setPhone( request.getPhone() );
        }
        if ( request.getEmail() != null ) {
            member.setEmail( request.getEmail() );
        }
        if ( request.getAddress() != null ) {
            member.setAddress( request.getAddress() );
        }
        if ( request.getMembershipPlan() != null ) {
            member.setMembershipPlan( request.getMembershipPlan() );
        }
        if ( request.getJoinDate() != null ) {
            member.setJoinDate( request.getJoinDate() );
        }
    }
}
