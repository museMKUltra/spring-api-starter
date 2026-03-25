package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.common.ErrorDto;
import com.codewithmosh.store.users.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/attendance")
class AttendanceController {
    private final UserService userService;
    private final AuthService authService;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceMapper attendanceMapper;
    private final AttendanceService attendanceService;
    private final AttendanceLabelRepository attendanceLabelRepository;

    AttendanceController(UserService userService, AuthService authService, AttendanceSessionRepository attendanceSessionRepository, AttendanceMapper attendanceMapper, AttendanceService attendanceService, AttendanceLabelRepository attendanceLabelRepository) {
        this.userService = userService;
        this.authService = authService;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceMapper = attendanceMapper;
        this.attendanceService = attendanceService;
        this.attendanceLabelRepository = attendanceLabelRepository;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(
            @Valid @RequestBody(required = false) ClockInRequest request
    ) {
        var user = authService.getCurrentUser();
        var now = LocalDateTime.now();

        var hasActiveSession = attendanceService.hasActiveSession(user);
        if (hasActiveSession) {
            return ResponseEntity.badRequest().body(
                    new ErrorDto("Active session already exists")
            );
        }

        var session = new AttendanceSession();
        session.setUser(user);
        session.setClockIn(now);
        session.setWorkDate(now.toLocalDate());
        session.setStatus(SessionStatus.ACTIVE);

        request = request == null ? new ClockInRequest() : request;

        var labelId = request.getLabelId();
        if (labelId != null) {
            var label = attendanceLabelRepository.findById(labelId).orElse(null);
            if (label == null) {
                return ResponseEntity.badRequest().body(
                        new ErrorDto("Label not found")
                );
            }
            session.setLabel(label);
        }

        var description = request.getDescription();
        if (description != null) {
            session.setDescription(description);
        }

        attendanceSessionRepository.save(session);

        return ResponseEntity.ok(attendanceMapper.toDto(session));
    }

    @GetMapping("/active-session")
    public ResponseEntity<ActiveSessionResponse> getActiveSession() {
        var session = attendanceService.getActiveSession(authService.getCurrentUser());

        return ResponseEntity.ok(session);
    }
}
