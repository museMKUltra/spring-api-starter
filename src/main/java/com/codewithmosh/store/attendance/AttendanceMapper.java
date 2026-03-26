package com.codewithmosh.store.attendance;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    SessionDto toDto(AttendanceSession session);
    LabelDto toLabelDto(AttendanceLabel label);
    EmployeeRateDto toEmployeeRateDto(EmployeeRate employeeRate);
}
