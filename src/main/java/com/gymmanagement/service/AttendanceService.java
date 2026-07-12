package com.gymmanagement.service;

import com.gymmanagement.entity.Attendance;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    Attendance checkIn(String memberId);
    Attendance checkOut(String memberId);
    List<Attendance> getTodaysAttendance();
    List<Attendance> getMonthlyAttendance(int year, int month);
    List<Attendance> getAttendanceByMember(String memberId);
}
