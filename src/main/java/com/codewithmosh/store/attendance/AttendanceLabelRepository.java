package com.codewithmosh.store.attendance;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AttendanceLabelRepository extends CrudRepository<AttendanceLabel, Long> {
    List<AttendanceLabel> findAll();
}