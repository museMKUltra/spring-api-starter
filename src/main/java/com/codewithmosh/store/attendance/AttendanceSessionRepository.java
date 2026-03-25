package com.codewithmosh.store.attendance;

import com.codewithmosh.store.users.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AttendanceSessionRepository extends CrudRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findByUserAndStatus(User user, SessionStatus status);
}