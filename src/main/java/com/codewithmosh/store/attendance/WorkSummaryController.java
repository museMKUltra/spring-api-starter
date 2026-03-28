package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.common.ErrorDto;
import com.codewithmosh.store.users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@Controller
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

    @GetMapping("/preview")
    public ResponseEntity<TrialSummaryDto> previewWorkSummary(
            @RequestParam Integer year,
            @RequestParam Short month
    ) {
        var summaryDto = attendanceService.previewWorkSummary(year, month);

        return ResponseEntity.ok(summaryDto);
    }

    @ExceptionHandler(WorkSummaryNotFoundException.class)
    public ResponseEntity<ErrorDto> handleBadRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(exception.getMessage()));
    }
}
