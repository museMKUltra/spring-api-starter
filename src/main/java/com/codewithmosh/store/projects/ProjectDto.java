package com.codewithmosh.store.projects;

import com.codewithmosh.store.attendance.AttendanceTime;
import lombok.Data;

import java.time.Instant;

@Data
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private Instant createdAt;

    public String getCreatedAt() {
        return createdAt != null
                ? new AttendanceTime(createdAt).getDateTimeInZone()
                : null;
    }
}
