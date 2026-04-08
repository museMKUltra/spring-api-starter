package com.codewithmosh.store.attendance;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record AttendanceTime(Instant utcInstant) {
    private static final ZoneId zone = ZoneId.of("Asia/Taipei");

    public AttendanceTime() {
        this(Instant.now());
    }

    public AttendanceTime(Instant utcInstant) {
        this.utcInstant = utcInstant;
    }

    /**
     * Gets the "Wall Clock" Date for a specific timezone.
     * Example: 2026-04-01
     */
    public LocalDate getDateInZone() {
        return utcInstant.atZone(zone).toLocalDate();
    }

    /**
     * Gets the "Wall Clock" Time for a specific timezone.
     * Example: 08:30:15
     */
    public LocalTime getTimeInZone() {
        return utcInstant.atZone(zone).toLocalTime();
    }

    /**
     * Returns a formatted string (e.g., "2026-04-01T08:30:00")
     * based on the user's timezone.
     */
    public String getDateTimeInZone() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return utcInstant.atZone(zone).format(formatter);
    }
}