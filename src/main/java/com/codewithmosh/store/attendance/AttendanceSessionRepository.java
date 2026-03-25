package com.codewithmosh.store.attendance;

import com.codewithmosh.store.users.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AttendanceSessionRepository extends CrudRepository<AttendanceSession, Long> {
    List<AttendanceSession> findByUserAndStatus(User user, SessionStatus status);
}