package com.codewithmosh.store.attendance;

import org.springframework.data.repository.CrudRepository;

public interface AttendanceSessionRepository extends CrudRepository<AttendanceSession, Long> {
}