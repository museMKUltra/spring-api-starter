package com.codewithmosh.store.attendance;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TrialSummaryDto {
    private final BigDecimal minutesPerHour = new BigDecimal("60");
    private List<AttendanceSession> sessions;
    private EmployeeRate employeeRate;
    @Getter
    private Long id;
    @Getter
    private Integer year;
    @Getter
    private Short month;
    private BigDecimal hourlyRate;
    private Long totalMinutes;

    public TrialSummaryDto(Long id, Integer year, Short month, BigDecimal hourlyRate, Long totalMinutes) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.hourlyRate = hourlyRate;
        this.totalMinutes = totalMinutes;
    }

    public TrialSummaryDto(Long id, Integer year, Short month, EmployeeRate employeeRate, List<AttendanceSession> sessions) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.employeeRate = employeeRate;
        this.sessions = sessions;
    }

    public BigDecimal getHourlyRate() {
        if (employeeRate == null) {
            return hourlyRate == null ? BigDecimal.ZERO : hourlyRate;
        }
        return employeeRate.getHourlyRate();
    }

    public Long getTotalMinutes() {
        if (sessions == null) {
            return totalMinutes == null ? 0 : totalMinutes;
        }

        return sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                .mapToLong(AttendanceSession::getWorkMinutes).sum();
    }

    public BigDecimal getTotalHours() {
        return BigDecimal.valueOf(getTotalMinutes())
                .divide(minutesPerHour, 10, RoundingMode.HALF_UP);
    }

    public BigDecimal getSalaryAmount() {
        return getTotalHours().multiply(getHourlyRate())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean hasActiveSessions() {
        return sessions != null && sessions.stream().anyMatch(s -> s.getStatus() == SessionStatus.ACTIVE);
    }
}
