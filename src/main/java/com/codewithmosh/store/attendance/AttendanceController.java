package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.common.ErrorDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@Controller
@RequestMapping("/attendance")
class AttendanceController {
    private final AuthService authService;
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<SessionDto> clockIn(
            @Valid @RequestBody(required = false) ClockInAndOutRequest request
    ) {
        request = request == null ? new ClockInAndOutRequest() : request;
        var sessionDto = attendanceService.clockIn(request.getLabelId(), request.getDescription());

        return ResponseEntity.ok(sessionDto);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<SessionDto> clockOut(
            @Valid @RequestBody(required = false) ClockInAndOutRequest request
    ) {
        request = request == null ? new ClockInAndOutRequest() : request;
        var sessionDto = attendanceService.clockOut(request.getLabelId(), request.getDescription());

        return ResponseEntity.ok(sessionDto);
    }

    @GetMapping("/active-session")
    public ResponseEntity<ActiveSessionResponse> getActiveSession() {
        var session = attendanceService.getActiveSession(authService.getCurrentUser());

        return ResponseEntity.ok(session);
    }

    @ExceptionHandler({LabelNotFoundException.class, ActiveSessionNotFoundException.class, ActiveSessionExistException.class, NotDraftWorkSummaryException.class})
    public ResponseEntity<ErrorDto> handleNotFound(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(exception.getMessage()));
    }
}
