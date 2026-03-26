package com.codewithmosh.store.attendance;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    SessionDto toDto(AttendanceSession session);

    EmployeeRateDto toEmployeeRateDto(EmployeeRate employeeRate);
}
