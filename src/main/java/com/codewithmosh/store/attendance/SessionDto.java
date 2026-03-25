package com.codewithmosh.store.attendance;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SessionDto {
    private Long id;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private LocalDate workDate;
    private Long workMinutes;
    private SessionStatus status;
    private String description;
    private LabelDto label;
}
