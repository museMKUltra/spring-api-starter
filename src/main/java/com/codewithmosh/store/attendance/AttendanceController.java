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
@RestController
@RequestMapping("/attendance")
class AttendanceController {
    private final AuthService authService;
    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<ActiveSessionResponse> clockIn(
            @Valid @RequestBody(required = false) ClockInAndOutRequest request
    ) {
        request = request == null ? new ClockInAndOutRequest() : request;
        var activeSessionResponse = attendanceService.clockIn(request.getLabelId(), request.getDescription());

        return ResponseEntity.ok(activeSessionResponse);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<ActiveSessionResponse> clockOut(
            @Valid @RequestBody(required = false) ClockInAndOutRequest request
    ) {
        request = request == null ? new ClockInAndOutRequest() : request;
        var activeSessionResponse = attendanceService.clockOut(request.getLabelId(), request.getDescription());

        return ResponseEntity.ok(activeSessionResponse);
    }

    @GetMapping("/active-session")
    public ResponseEntity<ActiveSessionResponse> getActiveSession() {
        var session = attendanceService.getActiveSession();

        return ResponseEntity.ok(session);
    }

    @ExceptionHandler({LabelNotFoundException.class, ActiveSessionNotFoundException.class, ActiveSessionExistException.class, DraftWorkSummaryNotFoundException.class, WorkSummaryHasBeenConfirmedException.class})
    public ResponseEntity<ErrorDto> handleBadRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(exception.getMessage()));
    }
}
