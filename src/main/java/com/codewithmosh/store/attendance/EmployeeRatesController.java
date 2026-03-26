package com.codewithmosh.store.attendance;

import com.codewithmosh.store.common.ErrorDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@AllArgsConstructor
@Controller
@RequestMapping("/employee-rates")
public class EmployeeRatesController {
    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<EmployeeRateDto> createEmployeeRate(
            @Valid @RequestBody CreateRateRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var employeeRateDto = attendanceService.createEmployeeRate(request.getHourlyRate());
        var uri = uriBuilder.path("/employee-rates/{id}").buildAndExpand(employeeRateDto.getId()).toUri();

        return ResponseEntity.created(uri).body(employeeRateDto);
    }

    @GetMapping("/{rateId}")
    public ResponseEntity<EmployeeRateDto> getEmployeeRate(
            @PathVariable Long rateId
    ) {
        var employeeRate = attendanceService.getEmployeeRate(rateId);

        return ResponseEntity.ok(employeeRate);
    }

    @ExceptionHandler(EmployeeRateNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEmployeeRateNotFound(Exception exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(exception.getMessage()));
    }
}
