package com.gymmanagement.repository;

import com.gymmanagement.entity.Attendance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByMemberId(String memberId);
    Optional<Attendance> findByMemberIdAndDate(String memberId, LocalDate date);
    List<Attendance> findByDateBetween(LocalDate start, LocalDate end);
}
