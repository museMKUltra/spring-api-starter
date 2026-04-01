package com.codewithmosh.store.attendance;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class SessionDto {
    private Long id;
    private Instant clockIn;
    private Instant clockOut;
    private LocalDate workDate;
    private Long workMinutes;
    private SessionStatus status;
    private String description;
    private LabelDto label;

    public String getClockIn() {
        return clockIn != null ? new AttendanceTime(clockIn).getDateTimeInZone() : null;
    }

    public String getClockOut() {
        return clockOut != null ? new AttendanceTime(clockOut).getDateTimeInZone() : null;
    }
}
