package com.codewithmosh.store.attendance;

import com.codewithmosh.store.users.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmployeeRateRepository extends CrudRepository<EmployeeRate, Long> {
    Optional<EmployeeRate> findByUserAndEffectiveToIsNull(User user);
}