package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.users.User;
import com.codewithmosh.store.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
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

    public List<SessionDto> getPeriodSessions(LocalDate startDate, LocalDate endDate) {
        var dateInZone = new AttendanceTime().getDateInZone();
        startDate = startDate == null ? dateInZone : startDate;
        endDate = endDate == null ? dateInZone.plusDays(1) : endDate;

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        var userId = AuthService.getCurrentUserId();
        var sessions = attendanceSessionRepository
                .getSessionsForPeriod(userId, startDate, endDate);

        return sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .sorted(Comparator.comparing(AttendanceSession::getClockIn))
                .map(attendanceMapper::toDto)
                .toList();
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

    @Transactional
    public SessionDto updateSession(Long sessionId, UpdateSessionRequest request) {
        var userId = AuthService.getCurrentUserId();
        var session = attendanceSessionRepository.findById(sessionId).orElse(null);

        if (session == null || !session.getUser().getId().equals(userId)) {
            throw new SessionNotFoundException();
        }

        var labelId = request.getLabelId();
        if (labelId != null) {
            updateSessionLabel(labelId, session);
        }

        if (request.getDescription() != null) {
            session.setDescription(request.getDescription());
        }

        if (request.getClockIn() != null) {
            session.setClockIn(request.getClockIn());
            session.setWorkDate(new AttendanceTime(request.getClockIn()).getDateInZone());
        }

        if (request.getClockOut() != null) {
            session.setClockOut(request.getClockOut());
        }

        if (session.getClockOut() != null && !session.getClockOut().isAfter(session.getClockIn())) {
            throw new IllegalArgumentException("clockOut must be after clockIn");
        }

        if (session.getStatus() == SessionStatus.COMPLETED
                && session.getClockIn() != null
                && session.getClockOut() != null) {
            session.setWorkMinutes(Duration.between(session.getClockIn(), session.getClockOut()).toMinutes());
        }

        attendanceSessionRepository.save(session);

        return attendanceMapper.toDto(session);
    }

    private void updateSession(Long labelId, String description, AttendanceSession session) {
        if (labelId != null) {
            updateSessionLabel(labelId, session);
        }

        if (description != null) {
            session.setDescription(description);
        }
    }

    private void updateSessionLabel(Long labelId, AttendanceSession session) {
        if (labelId == 0) {
            session.setLabel(null);
            return;
        }

        var label = attendanceLabelRepository.findById(labelId).orElse(null);
        if (label == null) {
            throw new LabelNotFoundException();
        }
        session.setLabel(label);
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

        return attendanceLabelRepository.getExistLabels(userId, true)
                .stream().map(attendanceMapper::toLabelDto).toList();
    }

    public LabelDto createLabel(String name, String color) {
        var user = authService.getCurrentUser();
        var hasExistName = attendanceLabelRepository.existsByUserIdAndName(user.getId(), name);
        if (hasExistName) {
            throw new LabelNameAlreadyExistException();
        }

        var maxSortOrder = attendanceLabelRepository.findMaxSortOrder(user.getId());
        var nextSortOrder = maxSortOrder == null ? 0 : maxSortOrder + 1;

        var label = new AttendanceLabel();
        label.setName(name);
        label.setColor(color);
        label.setType(LabelType.WORK);
        label.setSortOrder(nextSortOrder);

        user.addAttendanceLabel(label);
        attendanceLabelRepository.save(label);

        return attendanceMapper.toLabelDto(label);
    }

    public LabelDto updateLabel(Long id, String name, String color) {
        var userId = AuthService.getCurrentUserId();
        var label = attendanceLabelRepository.getExistLabel(userId, id).orElse(null);
        if (label == null) {
            throw new LabelNotFoundException();
        }

        if (name != null && !name.equals(label.getName())) {
            var hasExistName = attendanceLabelRepository.existsByName(name, id);
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
        var userId = AuthService.getCurrentUserId();
        var label = attendanceLabelRepository
                .getExistLabel(userId, id)
                .orElseThrow(LabelNotFoundException::new);

        label.setDeletedAt(Instant.now());
        label.setSortOrder(0);

        var remainingLabels = attendanceLabelRepository.getExistLabels(userId, false);
        for (int i = 0; i < remainingLabels.size(); i++) {
            remainingLabels.get(i).setSortOrder(i);
        }
    }

    @Transactional
    public void reorderLabels(List<Long> ids) {
        var userId = AuthService.getCurrentUserId();
        List<AttendanceLabel> labels = attendanceLabelRepository.findAllById(ids);

        // Safety check: ensure all belong to user
        for (AttendanceLabel l : labels) {
            var user = l.getUser();

            if (user == null) {
                throw new IllegalArgumentException("Global labels cannot be reordered");
            }

            if (!user.getId().equals(userId)) {
                throw new IllegalArgumentException("Invalid label ownership");
            }
        }

        // Assign new order
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            AttendanceLabel label = labels.stream()
                    .filter(l -> l.getId().equals(id))
                    .findFirst()
                    .orElseThrow(LabelNotFoundException::new);

            label.setSortOrder(i);
        }
    }
}
