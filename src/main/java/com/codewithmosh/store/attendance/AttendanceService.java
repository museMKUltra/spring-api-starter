package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.users.User;
import com.codewithmosh.store.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@AllArgsConstructor
@Service
class AttendanceService {
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceMapper attendanceMapper;
    private final AuthService authService;
    private final EmployeeRateRepository employeeRateRepository;
    private final AttendanceLabelRepository attendanceLabelRepository;
    private final WorkSummaryRepository workSummaryRepository;
    private final UserRepository userRepository;

    private List<AttendanceSession> getAttendanceSessions(SessionStatus status, Long userId) {
        return attendanceSessionRepository.findByUserIdAndStatus(userId, status);
    }

    @Transactional
    protected boolean hasActiveSessionAndAutoCancel(User user) {
        var sessions = getAttendanceSessions(SessionStatus.ACTIVE, user.getId());

        if (sessions.isEmpty()) {
            return false;
        }

        var lastIndex = sessions.size() - 1;
        for (int i = 0; i < lastIndex; i++) {
            sessions.get(i).setStatus(SessionStatus.CANCELLED);
            attendanceSessionRepository.save(sessions.get(i));
        }
        return true;
    }

    public AttendanceSession getAttendanceSession(SessionStatus status, Long userId) {
        var sessions = getAttendanceSessions(status, userId);

        return sessions.isEmpty() ? null : sessions.get(sessions.size() - 1);
    }

    public ActiveSessionResponse getActiveSession() {
        var userId = authService.getCurrentUserId();
        var session = getAttendanceSession(SessionStatus.ACTIVE, userId);

        return getActiveSessionResponse(session, userId);
    }

    private ActiveSessionResponse getActiveSessionResponse(AttendanceSession session, Long userId) {
        var hasSession = session != null;
        var workDate = hasSession ? session.getWorkDate() : LocalDate.now();
        var year = workDate.getYear();
        var month = (short) workDate.getMonthValue();

        var trialSummary = userRepository
                .findTrialSummary(userId, year, month, SummaryStatus.DRAFT)
                .orElse(null);

        var response = new ActiveSessionResponse();
        response.setActive(hasSession && session.getStatus() == SessionStatus.ACTIVE);
        response.setSession(attendanceMapper.toDto(session));
        response.setSummary(trialSummary);

        return response;
    }

    private LocalDateTime getClockTime() {
        var now = LocalDateTime.now();

        return now.truncatedTo(ChronoUnit.SECONDS);
    }

    @Transactional
    public EmployeeRateDto createEmployeeRate(BigDecimal hourlyRate) {
        var user = authService.getCurrentUser();
        var now = LocalDate.now();

        employeeRateRepository
                .findEffectiveRate(user.getId())
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

    public EmployeeRateDto getCurrentEmployeeRate(User user) {
        var employeeRate = employeeRateRepository.findEffectiveRate(user.getId()).orElse(null);

        if (employeeRate == null) {
            throw new EmployeeRateNotFoundException();
        }

        return attendanceMapper.toEmployeeRateDto(employeeRate);
    }

    @Transactional
    public ActiveSessionResponse clockIn(Long labelId, String description) {
        var user = authService.getCurrentUser();
        if (hasActiveSessionAndAutoCancel(user)) {
            throw new ActiveSessionExistException();
        }

        var session = AttendanceSession.createClockInSession(user);
        updateSession(labelId, description, session);
        attendanceSessionRepository.save(session);

        return getActiveSessionResponse(session, user.getId());
    }

    @Transactional
    public ActiveSessionResponse clockOut(Long labelId, String description) {
        var user = authService.getCurrentUser();
        var session = getAttendanceSession(SessionStatus.ACTIVE, user.getId());
        if (session == null) {
            throw new ActiveSessionNotFoundException();
        }

        AttendanceSession.updateClockOutSession(session);
        updateSession(labelId, description, session);
        findOrCreateWorkSummary(user, session);

        return getActiveSessionResponse(session, user.getId());
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

    public WorkSummaryDto getWorkSummary(Integer year, Short month) {
        var userId = AuthService.getCurrentUserId();
        var workSummary = workSummaryRepository.findWorkSummary(userId, year, month).orElse(null);
        if (workSummary == null) {
            throw new WorkSummaryNotFoundException();
        }

        return attendanceMapper.toWorkSummaryDto(workSummary);
    }

    private TrialSummaryDto getTrialSummary(Integer year, Short month, Long userId, WorkSummary summary) {
        var startDate = LocalDate.of(year, month, 1);
        var endDate = startDate.plusMonths(1);

        var totalMinutes = attendanceSessionRepository.calculateTotalWorkMinutes(userId, SessionStatus.COMPLETED, startDate, endDate);
        var hourlyRate = employeeRateRepository.getEffectiveHourlyRate(userId);

        return new TrialSummaryDto(summary.getId(), year, month, hourlyRate, totalMinutes);
    }

    public TrialSummaryDto previewWorkSummary(Integer year, Short month) {
        var userId = AuthService.getCurrentUserId();

        var summary = workSummaryRepository.findWorkSummaryWithStatus(userId, year, month, SummaryStatus.DRAFT).orElse(null);
        if (summary == null) {
            throw new WorkSummaryNotFoundException();
        }
        if (summary.getStatus() != SummaryStatus.DRAFT) {
            throw new NotDraftWorkSummaryException();
        }

        return getTrialSummary(year, month, userId, summary);
    }
}
