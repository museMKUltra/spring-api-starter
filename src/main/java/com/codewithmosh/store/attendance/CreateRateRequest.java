package com.codewithmosh.store.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRateRequest {
    @NotNull
    private BigDecimal hourlyRate;
}
