package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.common.ErrorDto;
import com.codewithmosh.store.users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/work-summary")
class WorkSummaryController {
    private final WorkSummaryRepository workSummaryRepository;
    private final AuthService authService;
    private final AttendanceMapper attendanceMapper;
    private final AttendanceService attendanceService;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final UserRepository userRepository;
    private final EmployeeRateRepository employeeRateRepository;

    @GetMapping
    public ResponseEntity<WorkSummaryDto> getWorkSummary(
            @RequestParam Integer year,
            @RequestParam Short month
    ) {
        var workSummaryDto = attendanceService.getWorkSummary(year, month);

        return ResponseEntity.ok(workSummaryDto);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<WorkSummaryDto>> getWorkSummaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var result = attendanceService.getWorkSummaries(page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/options")
    public ResponseEntity<List<WorkSummaryOption>> getWorkSummaryOptions() {
        var options = attendanceService.getWorkSummaryOptions();

        return ResponseEntity.ok(options);
    }

    @GetMapping("/preview")
    public ResponseEntity<TrialSummaryDto> previewWorkSummary(
            @RequestParam Integer year,
            @RequestParam Short month,
            @RequestParam(required = false) Long userId
    ) {
        // TODO: permission PREVIEW_ALL_WORK_SUMMARY
        var summaryDto = attendanceService.previewWorkSummary(year, month);

        return ResponseEntity.ok(summaryDto);
    }

    @PostMapping("/{summaryId}/confirm")
    public ResponseEntity<WorkSummaryDto> confirmWorkSummary(
            @PathVariable Long summaryId,
            @RequestParam(required = false) Long userId
    ) {
        // TODO: permission CONFIRM_OWN_WORK_SUMMARY
        // TODO: permission CONFIRM_ALL_WORK_SUMMARY
        var workSummaryDto = attendanceService.confirmWorkSummary(summaryId);

        return ResponseEntity.ok(workSummaryDto);
    }

    @ExceptionHandler({WorkSummaryNotFoundException.class, DraftWorkSummaryNotFoundException.class, ActiveSessionExistException.class})
    public ResponseEntity<ErrorDto> handleBadRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(exception.getMessage()));
    }
}
