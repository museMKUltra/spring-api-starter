package com.codewithmosh.store.attendance;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkSummaryDto {
    private Long id;
    private Integer year;
    private Short month;
    private Long totalMinutes;
    private SummaryStatus status;
    private BigDecimal hourlyRate;
    private BigDecimal salaryAmount;
}
