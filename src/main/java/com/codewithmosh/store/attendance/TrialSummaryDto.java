package com.codewithmosh.store.attendance;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TrialSummaryDto {
    private final BigDecimal minutesPerHour = new BigDecimal("60");

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

    public BigDecimal getHourlyRate() {
        return hourlyRate == null ? BigDecimal.ZERO : hourlyRate;
    }

    public Long getTotalMinutes() {
        return totalMinutes == null ? 0 : totalMinutes;
    }

    public BigDecimal getTotalHours() {
        return BigDecimal.valueOf(getTotalMinutes())
                .divide(minutesPerHour, 10, RoundingMode.HALF_UP);
    }

    public BigDecimal getSalaryAmount() {
        return getTotalHours().multiply(getHourlyRate())
                .setScale(2, RoundingMode.HALF_UP);
    }
}
