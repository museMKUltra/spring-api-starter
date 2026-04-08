package com.codewithmosh.store.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PeriodSessionsRequest {
    @NotNull(message = "startDate is required")
    private LocalDate startDate;
    @NotNull(message = "endDate is required")
    private LocalDate endDate;
}