package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.common.ErrorDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

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

    @GetMapping("/labels")
    public List<LabelDto> getLabels() {
        return attendanceService.getLabels();
    }

    @PostMapping("/labels")
    public ResponseEntity<LabelDto> createLabel(
            @Valid @RequestBody CreateLabelRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var labelDto = attendanceService.createLabel(request.getName(), request.getColor());
        var uri = uriBuilder.path("/api/attendance/labels/{id}").buildAndExpand(labelDto.getId()).toUri();

        return ResponseEntity.created(uri).body(labelDto);
    }

    @PutMapping("/labels/{id}")
    public ResponseEntity<LabelDto> updateLabel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLabelRequest request
    ) {
        var labelDto = attendanceService.updateLabel(id, request.getName(), request.getColor());

        return ResponseEntity.ok(labelDto);
    }

    @ExceptionHandler({LabelNotFoundException.class, ActiveSessionNotFoundException.class, ActiveSessionExistException.class, DraftWorkSummaryNotFoundException.class, WorkSummaryHasBeenConfirmedException.class, LabelNameAlreadyExistException.class})
    public ResponseEntity<ErrorDto> handleBadRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(exception.getMessage()));
    }
}
