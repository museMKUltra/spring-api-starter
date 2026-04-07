package com.codewithmosh.store.attendance;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceLabelRepository extends CrudRepository<AttendanceLabel, Long> {
    List<AttendanceLabel> findByUserId(Long userId);

    Optional<AttendanceLabel> findByUserIdAndId(Long userId, Long id);

    boolean existsByUserIdAndName(Long userId, String name);

    boolean existsByNameAndIdNot(String name, Long id);
}