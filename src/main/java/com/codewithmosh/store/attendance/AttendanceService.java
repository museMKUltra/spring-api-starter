package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.users.User;
import com.codewithmosh.store.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
        var trialSummary = getTrialDateSummary(workDate, userId);

        var response = new ActiveSessionResponse();
        response.setActive(hasSession && session.getStatus() == SessionStatus.ACTIVE);
        response.setSession(attendanceMapper.toDto(session));
        response.setSummary(trialSummary);

        return response;
    }

    @Transactional
    public EmployeeRateDto createEmployeeRate(BigDecimal hourlyRate) {
        var user = authService.getCurrentUser();
        var now = new AttendanceTime();

        getEffectiveRate(user.getId())
                .ifPresent(employeeRate -> employeeRate.setEffectiveTo(now.getDateInZone()));

        var employeeRate = new EmployeeRate();
        employeeRate.setEffectiveFrom(now.getDateInZone());
        employeeRate.setHourlyRate(hourlyRate);

        user.addEmployeeRate(employeeRate);
        employeeRateRepository.save(employeeRate);

        return attendanceMapper.toEmployeeRateDto(employeeRate);
    }

    private Optional<EmployeeRate> getEffectiveRate(Long userId) {
        var dateInZone = new AttendanceTime().getDateInZone();

        return employeeRateRepository.findEffectiveRate(userId, dateInZone);
    }

    public EmployeeRateDto getEmployeeRate(Long rateId) {
        var employeeRate = employeeRateRepository.findById(rateId).orElse(null);

        if (employeeRate == null) {
            throw new EmployeeRateNotFoundException();
        }

        return attendanceMapper.toEmployeeRateDto(employeeRate);
    }

    public EmployeeRateDto getCurrentEmployeeRate(User user) {
        var employeeRate = getEffectiveRate(user.getId()).orElse(null);

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

        var userId = user.getId();
        var session = AttendanceSession.createClockInSession(user);
        var workDate = session.getWorkDate();
        var year = workDate.getYear();
        var month = (short) workDate.getMonthValue();

        var workSummary = workSummaryRepository.findWorkSummary(userId, year, month).orElse(null);
        if (workSummary != null && workSummary.getStatus() != SummaryStatus.DRAFT) {
            throw new WorkSummaryHasBeenConfirmedException();
        }

        updateSession(labelId, description, session);
        attendanceSessionRepository.save(session);

        return getActiveSessionResponse(session, userId);
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
            throw new DraftWorkSummaryNotFoundException();
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

    private TrialSummaryDto getTrialSummary(Integer year, Short month, Long userId) {
        var startDate = LocalDate.of(year, month, 1);
        var endDate = startDate.plusMonths(1);

        var sessions = attendanceSessionRepository.getSessionsForPeriod(userId, startDate, endDate);
        var employeeRate = getEffectiveRate(userId).orElse(null);

        return new TrialSummaryDto(year, month, employeeRate, sessions);
    }

    private TrialSummaryDto getTrialDateSummary(LocalDate workDate, Long userId) {
        var year = workDate.getYear();
        var month = (short) workDate.getMonthValue();
        var date = (short) workDate.getDayOfMonth();

        var sessions = attendanceSessionRepository.findByUserIdAndWorkDate(userId, workDate);
        var employeeRate = getEffectiveRate(userId).orElse(null);

        return new TrialSummaryDto(year, month, date, employeeRate, sessions);
    }

    public TrialSummaryDto previewWorkSummary(Integer year, Short month) {
        var userId = AuthService.getCurrentUserId();

        return getTrialSummary(year, month, userId);
    }

    public WorkSummaryDto confirmWorkSummary(Long summaryId) {
        var summary = workSummaryRepository.findByIdAndStatus(summaryId, SummaryStatus.DRAFT).orElse(null);
        if (summary == null) {
            throw new DraftWorkSummaryNotFoundException();
        }

        var userId = AuthService.getCurrentUserId();
        var year = summary.getYear();
        var month = summary.getMonth();
        var trialSummary = getTrialSummary(year, month, userId);

        trialSummary.setId(summaryId);
        if (trialSummary.hasActiveSessions()) {
            throw new ActiveSessionExistException();
        }

        summary.setHourlyRate(trialSummary.getHourlyRate());
        summary.setTotalMinutes(trialSummary.getTotalMinutes());
        summary.setSalaryAmount(trialSummary.getSalaryAmount());
        summary.setStatus(SummaryStatus.CONFIRMED);
        workSummaryRepository.save(summary);

        return attendanceMapper.toWorkSummaryDto(summary);
    }

    public List<LabelDto> getLabels() {
        var userId = AuthService.getCurrentUserId();

        return attendanceLabelRepository.findByUserId(userId)
                .stream().map(attendanceMapper::toLabelDto).toList();
    }

    public LabelDto createLabel(String name, String color) {
        var hasExistName = attendanceLabelRepository.existsByName(name);
        if (hasExistName) {
            throw new LabelNameAlreadyExistException();
        }

        var label = new AttendanceLabel();
        label.setName(name);
        label.setColor(color);
        label.setType(LabelType.WORK);
        attendanceLabelRepository.save(label);

        return attendanceMapper.toLabelDto(label);
    }

    public LabelDto updateLabel(Long id, String name, String color) {
        var label = attendanceLabelRepository.findById(id).orElse(null);
        if (label == null) {
            throw new LabelNotFoundException();
        }

        if (name != null && !name.equals(label.getName())) {
            var hasExistName = attendanceLabelRepository.existsByNameAndIdNot(name, id);
            if (hasExistName) {
                throw new LabelNameAlreadyExistException();
            }
            label.setName(name);
        }

        if (color != null && !color.equals(label.getColor())) {
            label.setColor(color);
        }
        attendanceLabelRepository.save(label);

        return attendanceMapper.toLabelDto(label);
    }

    @Transactional
    public void deleteLabel(Long id) {
        if (!attendanceLabelRepository.existsById(id)) {
            throw new LabelNotFoundException();
        }

        attendanceLabelRepository.deleteById(id);
    }
}
