package com.codewithmosh.store.users;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MeDto extends UserDto {
    private BigDecimal hourlyRate;

    public MeDto(Long id, String name, String email, boolean guest, Instant expiresAt, BigDecimal hourlyRate) {
        super(id, name, email, guest, expiresAt);
        this.hourlyRate = hourlyRate;
    }
}
