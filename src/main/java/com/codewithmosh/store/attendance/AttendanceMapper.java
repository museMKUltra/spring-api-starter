package com.codewithmosh.store.attendance;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    SessionDto toDto(AttendanceSession session);

    EmployeeRateDto toEmployeeRateDto(EmployeeRate employeeRate);

    @Mapping(source = "hourlyRate", target = "hourlyRate", defaultValue = "0")
    @Mapping(source = "salaryAmount", target = "salaryAmount", defaultValue = "0")
    WorkSummaryDto toWorkSummaryDto(WorkSummary workSummary);
}
