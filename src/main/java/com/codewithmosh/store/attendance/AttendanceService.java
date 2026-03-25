package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.users.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
class AttendanceService {
    private final AuthService authService;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceMapper attendanceMapper;

    private AttendanceSession getAttendanceSession(SessionStatus status, User user) {
        var sessions = attendanceSessionRepository.findByUserAndStatus(user, status);

        return sessions.isEmpty() ? null : sessions.get(0);
    }

    public ActiveSessionResponse getActiveSession(User user) {
        var session = getAttendanceSession(SessionStatus.ACTIVE, user);

        var response = new ActiveSessionResponse();
        response.setActive(session != null);
        response.setSession(attendanceMapper.toDto(session));

        return response;
    }

    public boolean hasActiveSession(User user) {
        return getAttendanceSession(SessionStatus.ACTIVE, user) != null;
    }
}
