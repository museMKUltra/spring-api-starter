package com.codewithmosh.store.users;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SummaryDto {
    private final BigDecimal minutesPerHour = new BigDecimal("60");

    @Getter
    private Integer year;
    @Getter
    private Short month;

    private BigDecimal hourlyRate;
    private Long totalMinutes;

    public SummaryDto(Integer year, Short month, BigDecimal hourlyRate, Long totalMinutes) {
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
