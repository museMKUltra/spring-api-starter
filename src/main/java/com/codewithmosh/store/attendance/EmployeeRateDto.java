package com.codewithmosh.store.attendance;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRateDto {
    private Long id;
    private BigDecimal hourlyRate;
    private LocalDate effectiveFrom;
}
