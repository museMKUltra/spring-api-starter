package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.users.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@AllArgsConstructor
@Service
class AttendanceService {
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceMapper attendanceMapper;
    private final AuthService authService;
    private final EmployeeRateRepository employeeRateRepository;

    public AttendanceSession getAttendanceSession(SessionStatus status, User user) {
        var sessions = attendanceSessionRepository.findByUserAndStatus(user, status);

        return sessions.isEmpty() ? null : sessions.get(0);
    }

    public ActiveSessionResponse getActiveSession(User user) {
        var session = getAttendanceSession(SessionStatus.ACTIVE, user);

        var response = new ActiveSessionResponse();
        response.setActive(session != null);
        response.setSession(attendanceMapper.toDto(session));

        return response;
    }

    public boolean hasActiveSession(User user) {
        return getAttendanceSession(SessionStatus.ACTIVE, user) != null;
    }

    public LocalDateTime getClockTime() {
        var now = LocalDateTime.now();

        return now.truncatedTo(ChronoUnit.SECONDS);
    }

    @Transactional
    public EmployeeRateDto createEmployeeRate(BigDecimal hourlyRate) {
        var user = authService.getCurrentUser();
        var now = LocalDate.now();

        employeeRateRepository.findEffectiveRate(user)
                .ifPresent(employeeRate -> employeeRate.setEffectiveTo(now));

        var employeeRate = new EmployeeRate();
        employeeRate.setEffectiveFrom(now);
        employeeRate.setHourlyRate(hourlyRate);

        user.addEmployeeRate(employeeRate);
        employeeRateRepository.save(employeeRate);

        return attendanceMapper.toEmployeeRateDto(employeeRate);
    }

    public EmployeeRateDto getEmployeeRate(Long rateId) {
        var employeeRate = employeeRateRepository.findById(rateId).orElse(null);

        if (employeeRate == null) {
            throw new EmployeeRateNotFoundException();
        }

        return attendanceMapper.toEmployeeRateDto(employeeRate);
    }
}
