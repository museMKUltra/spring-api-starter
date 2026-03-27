package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.users.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
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
    private final AttendanceLabelRepository attendanceLabelRepository;
    private final WorkSummaryRepository workSummaryRepository;

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

    private boolean hasActiveSession(User user) {
        return getAttendanceSession(SessionStatus.ACTIVE, user) != null;
    }

    private LocalDateTime getClockTime() {
        var now = LocalDateTime.now();

        return now.truncatedTo(ChronoUnit.SECONDS);
    }

    @Transactional
    public EmployeeRateDto createEmployeeRate(BigDecimal hourlyRate) {
        var user = authService.getCurrentUser();
        var now = LocalDate.now();

        employeeRateRepository.findEffectiveRate(user).ifPresent(employeeRate -> employeeRate.setEffectiveTo(now));

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

    public EmployeeRateDto getCurrentEmployeeRate(User user) {
        var employeeRate = employeeRateRepository.findEffectiveRate(user).orElse(null);

        if (employeeRate == null) {
            throw new EmployeeRateNotFoundException();
        }

        return attendanceMapper.toEmployeeRateDto(employeeRate);
    }

    public SessionDto clockIn(Long labelId, String description) {
        var user = authService.getCurrentUser();
        var clockTime = getClockTime();

        if (hasActiveSession(user)) {
            throw new ActiveSessionExistException();
        }

        var session = new AttendanceSession();
        session.setUser(user);
        session.setClockIn(clockTime);
        session.setWorkDate(clockTime.toLocalDate());
        session.setStatus(SessionStatus.ACTIVE);

        updateSession(labelId, description, session);

        attendanceSessionRepository.save(session);

        return attendanceMapper.toDto(session);
    }

    @Transactional
    public SessionDto clockOut(Long labelId, String description) {
        var user = authService.getCurrentUser();
        var clockTime = getClockTime().plusMinutes(50);

        var session = getAttendanceSession(SessionStatus.ACTIVE, user);
        if (session == null) {
            throw new ActiveSessionNotFoundException();
        }

        var workMinutes = Duration.between(session.getClockIn(), clockTime).toMinutes();
        session.setClockOut(clockTime);
        session.setStatus(SessionStatus.COMPLETED);
        session.setWorkMinutes(workMinutes);

        updateSession(labelId, description, session);

        findOrCreateWorkSummary(user, session);

        return attendanceMapper.toDto(session);
    }

    private void findOrCreateWorkSummary(User user, AttendanceSession session) {
        var userId = user.getId();
        var year = session.getWorkDate().getYear();
        var month = (short) session.getWorkDate().getMonthValue();

        var workSummary = workSummaryRepository.findWorkSummary(userId, year, month).orElse(null);
        if (workSummary == null) {
            var newWorkSummary = new WorkSummary();
            newWorkSummary.setStatus(SummaryStatus.DRAFT);
            newWorkSummary.setUser(user);
            newWorkSummary.setYear(year);
            newWorkSummary.setMonth(month);
            newWorkSummary.setTotalMinutes(session.getWorkMinutes());

            user.addWorkSummary(newWorkSummary);
            workSummaryRepository.save(newWorkSummary);
            return;
        }

        if (workSummary.getStatus() != SummaryStatus.DRAFT) {
            throw new NotDraftWorkSummaryException();
        }

        workSummary.setTotalMinutes(workSummary.getTotalMinutes() + session.getWorkMinutes());
        workSummaryRepository.save(workSummary);
    }

    private void updateSession(Long labelId, String description, AttendanceSession session) {
        if (labelId != null) {
            var label = attendanceLabelRepository.findById(labelId).orElse(null);
            if (label == null) {
                throw new LabelNotFoundException();
            }
            session.setLabel(label);
        }

        if (description != null) {
            session.setDescription(description);
        }
    }
}
