package com.codewithmosh.store.attendance;

import com.codewithmosh.store.projects.ProjectDto;
import lombok.Data;

import java.util.List;

@Data
public class WorkSummaryOptionsDto {
    private List<ProjectDto> projects;
    private List<WorkSummaryPeriodDto> periods;
}