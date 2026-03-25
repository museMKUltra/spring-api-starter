package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.users.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/attendance")
class AttendanceController {
    private final UserService userService;
    private final AuthService authService;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceMapper attendanceMapper;

    AttendanceController(UserService userService, AuthService authService, AttendanceSessionRepository attendanceSessionRepository, AttendanceMapper attendanceMapper) {
        this.userService = userService;
        this.authService = authService;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceMapper = attendanceMapper;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceSession> clockIn() {
        var user = authService.getCurrentUser();
        var now = LocalDateTime.now();

        var session = new AttendanceSession();
        session.setUser(user);
        session.setClockIn(now);
        session.setWorkDate(now.toLocalDate());
        session.setStatus(SessionStatus.ACTIVE);

        attendanceSessionRepository.save(session);

        return ResponseEntity.ok(session);
    }

    @GetMapping("/active-session")
    public ResponseEntity<ActiveSessionResponse> getActiveSession() {
        var user = authService.getCurrentUser();
        var session = attendanceSessionRepository.findByUserAndStatus(user, SessionStatus.ACTIVE).orElse(null);

        var response = new ActiveSessionResponse();
        response.setActive(session != null);
        response.setSession(attendanceMapper.toDto(session));

        return ResponseEntity.ok(response);
    }
}
