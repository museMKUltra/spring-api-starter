package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.common.ErrorDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;

@AllArgsConstructor
@Controller
@RequestMapping("/attendance")
class AttendanceController {
    private final AuthService authService;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceMapper attendanceMapper;
    private final AttendanceService attendanceService;
    private final AttendanceLabelRepository attendanceLabelRepository;

    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(
            @Valid @RequestBody(required = false) ClockInAndOutRequest request
    ) {
        var user = authService.getCurrentUser();
        var clockTime = attendanceService.getClockTime();

        var hasActiveSession = attendanceService.hasActiveSession(user);
        if (hasActiveSession) {
            return ResponseEntity.badRequest().body(
                    new ErrorDto("Active session already exists")
            );
        }

        var session = new AttendanceSession();
        session.setUser(user);
        session.setClockIn(clockTime);
        session.setWorkDate(clockTime.toLocalDate());
        session.setStatus(SessionStatus.ACTIVE);

        request = request == null ? new ClockInAndOutRequest() : request;

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

    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(
            @Valid @RequestBody(required = false) ClockInAndOutRequest request
    ) {
        var user = authService.getCurrentUser();
        var clockTime = attendanceService.getClockTime();

        var session = attendanceService.getAttendanceSession(SessionStatus.ACTIVE, user);
        if (session == null) {
            return ResponseEntity.badRequest().body(
                    new ErrorDto("No active session found")
            );
        }

        var workMinutes = Duration.between(session.getClockIn(), clockTime).toMinutes();
        session.setClockOut(clockTime);
        session.setStatus(SessionStatus.COMPLETED);
        session.setWorkMinutes(workMinutes);

        request = request == null ? new ClockInAndOutRequest() : request;

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
