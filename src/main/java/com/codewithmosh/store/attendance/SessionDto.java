package com.codewithmosh.store.attendance;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SessionDto {
    private Long id;
    private LocalDateTime clockIn;
    private LocalDate workDate;
    private SessionStatus status;
    private String description;
    private LabelDto label;
}
