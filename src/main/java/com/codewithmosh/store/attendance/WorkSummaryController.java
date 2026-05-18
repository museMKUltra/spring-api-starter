package com.codewithmosh.store.attendance;

import com.codewithmosh.store.common.ErrorDto;
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
    private final AttendanceService attendanceService;

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
        var summaryDto = attendanceService.previewWorkSummary(year, month, userId);

        return ResponseEntity.ok(summaryDto);
    }

    @PostMapping("/{summaryId}/confirm")
    public ResponseEntity<WorkSummaryDto> confirmWorkSummary(
            @PathVariable Long summaryId
    ) {
        var workSummaryDto = attendanceService.confirmWorkSummary(summaryId);

        return ResponseEntity.ok(workSummaryDto);
    }

    @ExceptionHandler({WorkSummaryNotFoundException.class, DraftWorkSummaryNotFoundException.class, ActiveSessionExistException.class})
    public ResponseEntity<ErrorDto> handleBadRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(exception.getMessage()));
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ErrorDto> handlePermissionDenied(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDto(ex.getMessage()));
    }
}
