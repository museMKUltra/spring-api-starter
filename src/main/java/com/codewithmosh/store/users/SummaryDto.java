package com.codewithmosh.store.users;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@AllArgsConstructor
public class SummaryDto {
    private final BigDecimal minutesPerHour = new BigDecimal("60");
    private BigDecimal hourlyRate;
    private Long totalMinutes;

    public BigDecimal getHourlyRate() {
        return hourlyRate == null ? BigDecimal.ZERO : hourlyRate;
    }

    public Long getTotalMinutes() {
        return totalMinutes == null ? 0 : totalMinutes;
    }

    public BigDecimal getTotalHours() {
        return BigDecimal.valueOf(totalMinutes)
                .divide(minutesPerHour, 10, RoundingMode.HALF_UP);
    }

    public BigDecimal getSalaryAmount() {
        return getTotalHours().multiply(hourlyRate)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
