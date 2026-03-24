package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.users.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/attendance")
class AttendanceController {
    private final UserService userService;
    private final AuthService authService;
    private final AttendanceSessionRepository attendanceSessionRepository;

    AttendanceController(UserService userService, AuthService authService, AttendanceSessionRepository attendanceSessionRepository) {
        this.userService = userService;
        this.authService = authService;
        this.attendanceSessionRepository = attendanceSessionRepository;
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
}
