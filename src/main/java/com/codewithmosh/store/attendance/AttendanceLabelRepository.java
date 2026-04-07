package com.codewithmosh.store.attendance;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AttendanceLabelRepository extends CrudRepository<AttendanceLabel, Long> {
    List<AttendanceLabel> findByUserId(Long userId);

    boolean existsByUserIdAndName(Long userId, String name);

    boolean existsByNameAndIdNot(String name, Long id);
}