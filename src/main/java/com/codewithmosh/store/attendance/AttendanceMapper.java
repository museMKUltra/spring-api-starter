package com.codewithmosh.store.attendance;

import com.codewithmosh.store.projects.ProjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = ProjectMapper.class
)
public interface AttendanceMapper {
    @Mapping(source = "project.id", target = "projectId")
    SessionDto toDto(AttendanceSession session);

    EmployeeRateDto toEmployeeRateDto(EmployeeRate employeeRate);

    @Mapping(source = "hourlyRate", target = "hourlyRate", defaultValue = "0")
    @Mapping(source = "salaryAmount", target = "salaryAmount", defaultValue = "0")
    WorkSummaryDto toWorkSummaryDto(WorkSummary workSummary);

    LabelDto toLabelDto(AttendanceLabel label);
}
