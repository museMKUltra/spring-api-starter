package com.codewithmosh.store.attendance;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRateRepository extends CrudRepository<EmployeeRate, Long> {
    @Query("select e from EmployeeRate e where e.user.id = :userId and e.effectiveFrom <= current_date and e.effectiveTo is null")
    Optional<EmployeeRate> findEffectiveRate(@Param("userId") Long userId);
}