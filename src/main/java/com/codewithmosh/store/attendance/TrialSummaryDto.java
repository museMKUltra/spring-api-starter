package com.codewithmosh.store.attendance;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class TrialSummaryDto {
    private final BigDecimal minutesPerHour = new BigDecimal("60");
    private List<AttendanceSession> sessions;
    private EmployeeRate employeeRate;
    @Getter
    @Setter
    private Long id;
    @Getter
    private Integer year;
    @Getter
    private Short month;
    @Getter
    private Short date;
    private BigDecimal hourlyRate;
    private Long totalMinutes;

    public TrialSummaryDto(Long id, Integer year, Short month, BigDecimal hourlyRate, Long totalMinutes) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.hourlyRate = hourlyRate;
        this.totalMinutes = totalMinutes;
    }

    public TrialSummaryDto(Integer year, Short month, EmployeeRate employeeRate, List<AttendanceSession> sessions) {
        this.year = year;
        this.month = month;
        this.employeeRate = employeeRate;
        this.sessions = sessions;
    }

    public TrialSummaryDto(Integer year, Short month, Short date, EmployeeRate employeeRate, List<AttendanceSession> sessions) {
        this.year = year;
        this.month = month;
        this.date = date;
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

        return getCountableSessions()
                .mapToLong(AttendanceSession::getWorkMinutes).sum();
    }

    private Stream<AttendanceSession> getCountableSessions() {
        return sessions.stream().filter(this::isCountableSession);
    }

    private boolean isCountableSession(AttendanceSession s) {
        var isGlobalLabel = s.getLabel() != null && s.getLabel().getUser() == null;

        return s.getStatus() == SessionStatus.COMPLETED && !isGlobalLabel;
    }

    private Stream<AttendanceSession> getCompletedSessions() {
        return sessions.stream().filter(this::isCompletedSession);
    }

    private boolean isCompletedSession(AttendanceSession s) {
        return s.getStatus() == SessionStatus.COMPLETED;
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

    public List<TrialSummaryLabelDto> getLabels() {
        if (sessions == null) {
            return new ArrayList<>();
        }

        var labelMap = new HashMap<Long, TrialSummaryLabelDto>();
        var completedSessions = getCompletedSessions();

        completedSessions.forEach(session -> {
            var labelId = session.getLabel() == null ? 0L : session.getLabel().getId();
            var labelDto = labelMap.computeIfAbsent(labelId, id -> new TrialSummaryLabelDto(labelId, 0L));
            var workMinutes = session.getWorkMinutes() == null ? 0L : session.getWorkMinutes();

            labelDto.setWorkMinutes(labelDto.getWorkMinutes() + workMinutes);
        });

        return new ArrayList<>(labelMap.values());
    }
}
