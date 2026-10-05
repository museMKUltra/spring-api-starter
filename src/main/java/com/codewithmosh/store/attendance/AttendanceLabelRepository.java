package com.codewithmosh.store.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceLabelRepository extends JpaRepository<AttendanceLabel, Long> {
    @Query("select a from AttendanceLabel a " +
            "where (a.project.id = :projectId or (:includeGlobal = true and a.project is null)) and a.deletedAt is null " +
            "order by case when a.project is null then 0 else 1 end, a.sortOrder asc")
    List<AttendanceLabel> getExistLabels(@Param("projectId") Long projectId, @Param("includeGlobal") boolean includeGlobal);

    @Query("select a from AttendanceLabel a where a.project.id = :projectId and a.id = :id and a.deletedAt is null")
    Optional<AttendanceLabel> getExistLabel(@Param("projectId") Long projectId, @Param("id") Long id);

    @Query("select a from AttendanceLabel a where (a.project.id = :projectId or a.project is null) and a.id = :id and a.deletedAt is null")
    Optional<AttendanceLabel> getExistProjectOrGlobalLabel(@Param("projectId") Long projectId, @Param("id") Long id);

    @Query("select (count(a) > 0) from AttendanceLabel a where a.project.id = :projectId and a.name = :name and a.deletedAt is null")
    boolean existsByName(@Param("projectId") Long projectId, @Param("name") String name);

    @Query("select (count(a) > 0) from AttendanceLabel a where a.project.id = :projectId and a.name = :name and a.id <> :id and a.deletedAt is null")
    boolean existsByName(@Param("projectId") Long projectId, @Param("name") String name, @Param("id") Long id);

    @Query("select MAX(a.sortOrder) from AttendanceLabel a where a.project.id = :projectId and a.deletedAt is null")
    Integer findMaxSortOrder(@Param("projectId") Long projectId);
}
