package com.codewithmosh.store.attendance;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceSessionRepository extends CrudRepository<AttendanceSession, Long> {
    @EntityGraph(attributePaths = "label")
    List<AttendanceSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    @Query("select a from AttendanceSession a " +
            "where a.user.id = :userId " +
            "and a.workDate >= :startDate " +
            "and a.workDate < :endDate")
    List<AttendanceSession> getSessionsForPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}