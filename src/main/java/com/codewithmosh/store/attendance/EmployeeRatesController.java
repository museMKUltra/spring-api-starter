package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.users.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

@AllArgsConstructor
@Controller
@RequestMapping("/employee-rates")
public class EmployeeRatesController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final EmployeeRateRepository employeeRateRepository;
    private final AttendanceMapper attendanceMapper;

    @Transactional
    @PostMapping
    public ResponseEntity<EmployeeRateDto> createEmployeeRate(
            @Valid @RequestBody CreateRateRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var user = authService.getCurrentUser();
        var now = LocalDate.now();

        employeeRateRepository.findByUserAndEffectiveToIsNull(user).ifPresent(employeeRate -> employeeRate.setEffectiveTo(now));

        var employeeRate = new EmployeeRate();
        employeeRate.setEffectiveFrom(now);
        employeeRate.setHourlyRate(request.getHourlyRate());

        user.addEmployeeRate(employeeRate);
        userRepository.save(user);

        var uri = uriBuilder.path("/employee-rates/{id}").buildAndExpand(employeeRate.getId()).toUri();

        return ResponseEntity.created(uri).body(attendanceMapper.toEmployeeRateDto(employeeRate));
    }
}
