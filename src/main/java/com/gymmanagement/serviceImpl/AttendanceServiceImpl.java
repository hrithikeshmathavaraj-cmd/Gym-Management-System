package com.gymmanagement.serviceImpl;

import com.gymmanagement.entity.Attendance;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.exception.UnauthorizedActionException;
import com.gymmanagement.repository.AttendanceRepository;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.service.AttendanceService;
import com.gymmanagement.service.realtime.RealtimeDashboardPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Handles daily attendance via QR check-in/check-out. A member may only have
 * one attendance record per calendar day.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final RealtimeDashboardPublisher realtimeDashboardPublisher;

    @Override
    @Transactional
    public Attendance checkIn(String memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }

        LocalDate today = LocalDate.now();
        attendanceRepository.findByMemberIdAndDate(memberId, today).ifPresent(a -> {
            throw new UnauthorizedActionException("Member has already checked in today");
        });

        Attendance attendance = Attendance.builder()
                .memberId(memberId)
                .date(today)
                .checkIn(LocalDateTime.now())
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Member checked in: {}", memberId);
        realtimeDashboardPublisher.publish("ATTENDANCE_CHECK_IN");
        return saved;
    }

    @Override
    @Transactional
    public Attendance checkOut(String memberId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByMemberIdAndDate(memberId, today)
                .orElseThrow(() -> new ResourceNotFoundException("No check-in record found for today"));

        if (attendance.getCheckOut() != null) {
            throw new UnauthorizedActionException("Member has already checked out today");
        }

        attendance.setCheckOut(LocalDateTime.now());
        Attendance saved = attendanceRepository.save(attendance);
        log.info("Member checked out: {}", memberId);
        realtimeDashboardPublisher.publish("ATTENDANCE_CHECK_OUT");
        return saved;
    }

    @Override
    public List<Attendance> getTodaysAttendance() {
        return attendanceRepository.findByDate(LocalDate.now());
    }

    @Override
    public List<Attendance> getMonthlyAttendance(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return attendanceRepository.findByDateBetween(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    @Override
    public List<Attendance> getAttendanceByMember(String memberId) {
        return attendanceRepository.findByMemberId(memberId);
    }
}
