package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.common.ErrorDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@AllArgsConstructor
@Controller
@RequestMapping("/work-summary")
class WorkSummaryController {
    private final WorkSummaryRepository workSummaryRepository;
    private final AuthService authService;
    private final AttendanceMapper attendanceMapper;

    @GetMapping
    public ResponseEntity<?> getWorkSummary(
            @RequestParam Integer year,
            @RequestParam Short month
    ) {
        var user = authService.getCurrentUser();
        var workSummary = workSummaryRepository.findWorkSummary(user.getId(), year, month).orElse(null);
        if (workSummary == null) {
            return ResponseEntity.badRequest().body(new ErrorDto("Work summary not found"));
        }

        return ResponseEntity.ok(attendanceMapper.toWorkSummaryDto(workSummary));
    }
}
