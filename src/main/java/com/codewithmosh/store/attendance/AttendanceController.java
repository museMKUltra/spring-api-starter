package com.codewithmosh.store.attendance;

import com.codewithmosh.store.common.ErrorDto;
import com.codewithmosh.store.projects.ProjectArchivedException;
import com.codewithmosh.store.projects.ProjectNotFoundException;
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

    @PostMapping("/projects/{projectId}/clock-in")
    public ResponseEntity<ActiveSessionResponse> clockIn(
            @PathVariable Long projectId,
            @Valid @RequestBody(required = false) ClockInAndOutRequest request
    ) {
        request = request == null ? new ClockInAndOutRequest() : request;
        var activeSessionResponse = attendanceService.clockIn(projectId, request.getLabelId(), request.getDescription());

        return ResponseEntity.ok(activeSessionResponse);
    }

    @PostMapping("/projects/{projectId}/clock-out")
    public ResponseEntity<ActiveSessionResponse> clockOut(
            @PathVariable Long projectId,
            @Valid @RequestBody(required = false) ClockInAndOutRequest request
    ) {
        request = request == null ? new ClockInAndOutRequest() : request;
        var activeSessionResponse = attendanceService.clockOut(projectId, request.getLabelId(), request.getDescription());

        return ResponseEntity.ok(activeSessionResponse);
    }

    @GetMapping("/projects/{projectId}/period-sessions")
    public ResponseEntity<List<SessionDto>> getPeriodSessions(
            @PathVariable Long projectId,
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate
    ) {
        var sessions = attendanceService.getPeriodSessions(projectId, startDate, endDate);

        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/projects/{projectId}/active-session")
    public ResponseEntity<ActiveSessionResponse> getActiveSession(@PathVariable Long projectId) {
        var session = attendanceService.getActiveSession(projectId);

        return ResponseEntity.ok(session);
    }

    @PostMapping("/projects/{projectId}/sessions")
    public ResponseEntity<SessionDto> createSession(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateSessionRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var session = attendanceService.createSession(projectId, request);

        var uri = uriBuilder
                .path("/api/attendance/sessions/{id}")
                .buildAndExpand(session.getId())
                .toUri();

        return ResponseEntity.created(uri).body(session);
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

    @GetMapping("/projects/{projectId}/labels")
    public List<LabelDto> getLabels(@PathVariable Long projectId) {
        return attendanceService.getLabels(projectId);
    }

    @PostMapping("/projects/{projectId}/labels")
    public ResponseEntity<LabelDto> createLabel(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateLabelRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var labelDto = attendanceService.createLabel(projectId, request.getName(), request.getColor());
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

    @PostMapping("/projects/{projectId}/labels/reorder")
    public void reorder(
            @PathVariable Long projectId,
            @Valid @RequestBody ReorderLabelsRequest request
    ) {
        attendanceService.reorderLabels(projectId, List.of(request.getIds()));
    }

    @ExceptionHandler({LabelNotFoundException.class, ActiveSessionNotFoundException.class, ActiveSessionExistException.class, DraftWorkSummaryNotFoundException.class, WorkSummaryHasBeenConfirmedException.class, LabelNameAlreadyExistException.class, SessionNotFoundException.class, IllegalArgumentException.class, ProjectNotFoundException.class, ProjectArchivedException.class})
    public ResponseEntity<ErrorDto> handleBadRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(exception.getMessage()));
    }
}
