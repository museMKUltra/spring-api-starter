package com.codewithmosh.store.users;

import com.codewithmosh.store.attendance.SummaryStatus;
import com.codewithmosh.store.attendance.TrialSummaryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("select new com.codewithmosh.store.users.MeDto(u.id, u.name, u.email, er.hourlyRate) from User u " +
            "left join u.employeeRates er " +
            "on er.effectiveFrom <= current_date and er.effectiveTo is null " +
            "where u.id = :userId"
    )
    Optional<MeDto> findMe(@Param("userId") Long userId);

    @Query("select u from User u where u.email = ?1")
    Optional<User> findByEmail(String email);

    @Query("select new com.codewithmosh.store.attendance.TrialSummaryDto(ws.id, :year, :month, er.hourlyRate, ws.totalMinutes) from User u " +
            "left join u.employeeRates er " +
            "on er.effectiveFrom <= current_date and er.effectiveTo is null " +
            "left join u.workSummaries ws " +
            "on ws.year = :year and ws.month = :month and ws.status = :status " +
            "where u.id = :userId"
    )
    Optional<TrialSummaryDto> findTrialSummary(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Short month,
            @Param("status") SummaryStatus status
    );
}
