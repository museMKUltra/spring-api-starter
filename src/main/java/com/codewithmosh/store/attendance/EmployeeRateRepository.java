package com.codewithmosh.store.attendance;

import com.codewithmosh.store.users.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmployeeRateRepository extends CrudRepository<EmployeeRate, Long> {
    @Query("select e from EmployeeRate e where e.user = ?1 and e.effectiveTo is null")
    Optional<EmployeeRate> findEffectiveRate(User user);
}