package com.codewithmosh.store.users;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeDto extends UserDto {
    private BigDecimal hourlyRate;

    public MeDto(Long id, String name, String email, BigDecimal hourlyRate) {
        super(id, name, email);
        this.hourlyRate = hourlyRate == null ? BigDecimal.ZERO : hourlyRate;
    }
}
