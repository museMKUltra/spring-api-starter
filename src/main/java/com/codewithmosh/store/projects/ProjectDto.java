package com.codewithmosh.store.projects;

import lombok.Data;

import java.time.Instant;

@Data
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private Instant createdAt;
}
