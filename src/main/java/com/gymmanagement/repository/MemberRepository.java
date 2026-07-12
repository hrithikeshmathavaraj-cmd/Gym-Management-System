package com.gymmanagement.repository;

import com.gymmanagement.entity.Member;
import com.gymmanagement.entity.MembershipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {

    Optional<Member> findByMemberCode(String memberCode);

    boolean existsByEmail(String email);

    Page<Member> findByNameContainingIgnoreCaseOrMemberCodeContainingIgnoreCaseOrPhoneContaining(
            String name, String memberCode, String phone, Pageable pageable);

    List<Member> findByStatus(MembershipStatus status);

    long countByStatus(MembershipStatus status);

    List<Member> findByExpiryDateBetween(LocalDate start, LocalDate end);

    List<Member> findByExpiryDateBefore(LocalDate date);
}
