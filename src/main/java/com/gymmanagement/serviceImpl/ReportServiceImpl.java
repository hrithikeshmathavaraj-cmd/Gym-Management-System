package com.gymmanagement.serviceImpl;

import com.gymmanagement.dto.DashboardStatsResponse;
import com.gymmanagement.dto.PaymentResponse;
import com.gymmanagement.entity.Member;
import com.gymmanagement.entity.MembershipStatus;
import com.gymmanagement.entity.Payment;
import com.gymmanagement.entity.PaymentStatus;
import com.gymmanagement.repository.*;
import com.gymmanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates data across members, payments, attendance, trainers and
 * equipment to power the dashboard analytics view (revenue, distribution,
 * attendance and expiry charts).
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final AttendanceRepository attendanceRepository;
    private final TrainerRepository trainerRepository;
    private final EquipmentRepository equipmentRepository;
    private final PlanRepository planRepository;

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy");

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long totalMembers = memberRepository.count();
        long activeMembers = memberRepository.countByStatus(MembershipStatus.ACTIVE);
        long expiredMembers = memberRepository.countByStatus(MembershipStatus.EXPIRED);
        long todaysAttendance = attendanceRepository.findByDate(LocalDate.now()).size();

        List<Payment> allPayments = paymentRepository.findAll();
        double totalRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payment::getAmount).sum();

        YearMonth currentMonth = YearMonth.now();
        double monthlyRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .filter(p -> YearMonth.from(p.getPaymentDate()).equals(currentMonth))
                .mapToDouble(Payment::getAmount).sum();

        List<PaymentResponse> recentPayments = paymentRepository.findTop10ByOrderByPaymentDateDesc().stream()
                .map(p -> PaymentResponse.builder()
                        .id(p.getId())
                        .memberId(p.getMemberId())
                        .memberName(memberRepository.findById(p.getMemberId()).map(Member::getName).orElse("Unknown"))
                        .amount(p.getAmount())
                        .paymentDate(p.getPaymentDate())
                        .paymentMethod(p.getPaymentMethod())
                        .status(p.getStatus())
                        .transactionId(p.getTransactionId())
                        .build())
                .toList();

        // Membership distribution by plan name
        Map<String, Long> membershipDistribution = new LinkedHashMap<>();
        memberRepository.findAll().forEach(m -> {
            String planName = planRepository.findById(m.getMembershipPlan())
                    .map(com.gymmanagement.entity.Plan::getPlanName).orElse("Unknown");
            membershipDistribution.merge(planName, 1L, Long::sum);
        });

        // Last 6 months revenue chart
        Map<String, Double> monthlyRevenueChart = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            double revenue = allPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                    .filter(p -> YearMonth.from(p.getPaymentDate()).equals(ym))
                    .mapToDouble(Payment::getAmount).sum();
            monthlyRevenueChart.put(ym.format(MONTH_LABEL), revenue);
        }

        // Last 7 days attendance chart
        Map<String, Long> attendanceChart = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            long count = attendanceRepository.findByDate(day).size();
            attendanceChart.put(day.toString(), count);
        }

        return DashboardStatsResponse.builder()
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .expiredMembers(expiredMembers)
                .todaysAttendance(todaysAttendance)
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .totalTrainers(trainerRepository.count())
                .totalEquipment(equipmentRepository.count())
                .recentPayments(recentPayments)
                .membershipDistribution(membershipDistribution)
                .monthlyRevenueChart(monthlyRevenueChart)
                .attendanceChart(attendanceChart)
                .build();
    }

    @Override
    public List<Member> getExpiredMembers() {
        return memberRepository.findByStatus(MembershipStatus.EXPIRED);
    }

    @Override
    public List<Member> getActiveMembers() {
        return memberRepository.findByStatus(MembershipStatus.ACTIVE);
    }

    @Override
    public List<Member> getExpiringMembers(int withinDays) {
        LocalDate today = LocalDate.now();
        return memberRepository.findByExpiryDateBetween(today, today.plusDays(withinDays));
    }
}
