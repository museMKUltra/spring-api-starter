package com.codewithmosh.store.attendance;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceLabelRepository extends CrudRepository<AttendanceLabel, Long> {
    List<AttendanceLabel> findByUserId(Long userId);

    Optional<AttendanceLabel> findByUserIdAndId(Long userId, Long id);

    @Query("select a from AttendanceLabel a where a.user.id = :userId and a.id = :id and a.deletedAt is null")
    Optional<AttendanceLabel> getExistLabel(@Param("userId") Long userId, @Param("id") Long id);

    boolean existsByUserIdAndName(Long userId, String name);

    boolean existsByNameAndIdNot(String name, Long id);
}