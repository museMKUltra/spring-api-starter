package com.codewithmosh.store.attendance;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WorkSummaryRepository extends CrudRepository<WorkSummary, Long> {
    @EntityGraph(attributePaths = "user")
    @Query("select w from WorkSummary w where w.user.id = :userId and w.year = :year and w.month = :month")
    Optional<WorkSummary> findWorkSummary(@Param("userId") Long userId, @Param("year") Integer year, @Param("month") Short month);

    @Query("select w from WorkSummary w where w.user.id = :userId and w.year = :year and w.month = :month and w.status = :status")
    Optional<WorkSummary> findWorkSummaryWithStatus(@Param("userId") Long userId, @Param("year") Integer year, @Param("month") Short month, @Param("status") SummaryStatus status);
}