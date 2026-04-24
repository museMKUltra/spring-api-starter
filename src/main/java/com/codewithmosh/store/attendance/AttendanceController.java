package com.codewithmosh.store.attendance;

import com.codewithmosh.store.common.ErrorDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/attendance")
class AttendanceController {
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

    @GetMapping("/period-sessions")
    public ResponseEntity<List<SessionDto>> getPeriodSessions(
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate
    ) {
        var sessions = attendanceService.getPeriodSessions(startDate, endDate);

        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/active-session")
    public ResponseEntity<ActiveSessionResponse> getActiveSession() {
        var session = attendanceService.getActiveSession();

        return ResponseEntity.ok(session);
    }

    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionDto> updateSession(
            @PathVariable(name = "sessionId") Long id,
            @Valid @RequestBody UpdateSessionRequest request
    ) {
        var session = attendanceService.updateSession(id, request);

        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable(name = "sessionId") Long id
    ) {
        attendanceService.deleteSession(id);

        return ResponseEntity.noContent().build();
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

    @DeleteMapping("/labels/{id}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        attendanceService.deleteLabel(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/labels/reorder")
    public void reorder(@Valid @RequestBody ReorderLabelsRequest request) {
        attendanceService.reorderLabels(List.of(request.getIds()));
    }

    @ExceptionHandler({LabelNotFoundException.class, ActiveSessionNotFoundException.class, ActiveSessionExistException.class, DraftWorkSummaryNotFoundException.class, WorkSummaryHasBeenConfirmedException.class, LabelNameAlreadyExistException.class, SessionNotFoundException.class, IllegalArgumentException.class, SessionNotFoundException.class})
    public ResponseEntity<ErrorDto> handleBadRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(exception.getMessage()));
    }
}
