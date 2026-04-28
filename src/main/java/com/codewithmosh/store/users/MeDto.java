package com.codewithmosh.store.users;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeDto extends UserDto {
    private BigDecimal hourlyRate;

    public MeDto(Long id, String name, String email, boolean isGuest, BigDecimal hourlyRate) {
        super(id, name, email, isGuest);
        this.hourlyRate = hourlyRate == null ? BigDecimal.ZERO : hourlyRate;
    }
}
