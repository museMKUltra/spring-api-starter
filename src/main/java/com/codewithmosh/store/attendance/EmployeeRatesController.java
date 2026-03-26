package com.codewithmosh.store.attendance;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
