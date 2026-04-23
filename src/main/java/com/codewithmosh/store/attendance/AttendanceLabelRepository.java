package com.codewithmosh.store.attendance;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceLabelRepository extends CrudRepository<AttendanceLabel, Long> {
    @Query("select a from AttendanceLabel a where (a.user.id = :userId or a.user.id is null) and a.deletedAt is null order by a.user.id nulls first, a.sortOrder asc")
    List<AttendanceLabel> getExistLabels(@Param("userId") Long userId);

    @Query("select a from AttendanceLabel a where a.user.id = :userId and a.id = :id and a.deletedAt is null")
    Optional<AttendanceLabel> getExistLabel(@Param("userId") Long userId, @Param("id") Long id);

    boolean existsByUserIdAndName(Long userId, String name);

    @Query("select (count(a) > 0) from AttendanceLabel a where a.name = :name and a.id <> :id and a.deletedAt is null")
    boolean existsByName(@Param("name") String name, @Param("id") Long id);

    @Query("select MAX(a.sortOrder) from AttendanceLabel a where a.user.id = :userId and a.deletedAt is null")
    Integer findMaxSortOrder(@Param("userId") Long userId);
}