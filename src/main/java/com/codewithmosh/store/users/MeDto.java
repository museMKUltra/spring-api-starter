package com.codewithmosh.store.users;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MeDto extends UserDto {
    private BigDecimal hourlyRate;

    public MeDto(Long id, String name, String email, boolean guest, Instant expiresAt, Role role, BigDecimal hourlyRate) {
        super(id, name, email, guest, expiresAt, role);
        this.hourlyRate = hourlyRate;
    }

    public void hideHourlyRate() {
        hourlyRate = null;
    }
}
