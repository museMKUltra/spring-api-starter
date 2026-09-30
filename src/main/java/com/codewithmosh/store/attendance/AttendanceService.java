package com.codewithmosh.store.attendance;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.projects.Project;
import com.codewithmosh.store.projects.ProjectArchivedException;
import com.codewithmosh.store.projects.ProjectNotFoundException;
import com.codewithmosh.store.projects.ProjectRepository;
import com.codewithmosh.store.users.Permission;
import com.codewithmosh.store.users.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
class AttendanceService {
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceMapper attendanceMapper;
    private final AuthService authService;
    private final EmployeeRateRepository employeeRateRepository;
    private final AttendanceLabelRepository attendanceLabelRepository;
    private final WorkSummaryRepository workSummaryRepository;
    private final ProjectRepository projectRepository;

    private Project getProject(Long projectId, Long userId) {
        return projectRepository
                .findByIdAndUserId(projectId, userId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private Project getActiveProject(Long projectId, Long userId) {
        var project = getProject(projectId, userId);
        if (project.isArchived()) {
            throw new ProjectArchivedException();
        }

        return project;
    }

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

    private AttendanceSession getProjectActiveSession(Long userId, Long projectId) {
        var session = getAttendanceSession(SessionStatus.ACTIVE, userId);

        return session != null && session.getProject().getId().equals(projectId) ? session : null;
    }

    public ActiveSessionResponse getActiveSession(Long projectId) {
        var user = authService.getCurrentUser();
        getProject(projectId, user.getId());
        var session = getProjectActiveSession(user.getId(), projectId);

        return getActiveSessionResponse(session, user);
    }

    public List<SessionDto> getPeriodSessions(Long projectId, LocalDate startDate, LocalDate endDate) {
        var dateInZone = new AttendanceTime().getDateInZone();
        startDate = startDate == null ? dateInZone : startDate;
        endDate = endDate == null ? dateInZone.plusDays(1) : endDate;

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        var userId = AuthService.getCurrentUserId();
        getProject(projectId, userId);
        var sessions = attendanceSessionRepository
                .getProjectSessionsForPeriod(projectId, startDate, endDate);

        return sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .sorted(Comparator.comparing(AttendanceSession::getClockIn))
                .map(attendanceMapper::toDto)
                .toList();
    }

    private ActiveSessionResponse getActiveSessionResponse(AttendanceSession session, User user) {
        var hasSession = session != null;
        var workDate = hasSession ? session.getWorkDate() : LocalDate.now();
        var trialSummary = getTrialDateSummary(workDate, user.getId());

        var response = new ActiveSessionResponse();
        response.setActive(hasSession && session.getStatus() == SessionStatus.ACTIVE);
        response.setSession(attendanceMapper.toDto(session));
        response.setSummary(trialSummary);

        if (!user.hasPermission(Permission.MANAGE_OWN_HOURLY_RATE)) {
            response.getSummary().hideHourlyRate();
        }

        return response;
    }

    @Transactional
    public EmployeeRateDto createEmployeeRate(BigDecimal hourlyRate) {
        var user = authService.getCurrentUser();
        if (!user.hasPermission(Permission.MANAGE_OWN_HOURLY_RATE)) {
            throw new PermissionDeniedException("You don't have permission to create own hourly rate");
        }

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
        var user = authService.getCurrentUser();
        if (!user.hasPermission(Permission.MANAGE_ALL_HOURLY_RATE)) {
            throw new PermissionDeniedException("You don't have permission to manage hourly rate");
        }

        var employeeRate = employeeRateRepository.findById(rateId).orElse(null);

        if (employeeRate == null) {
            throw new EmployeeRateNotFoundException();
        }

        return attendanceMapper.toEmployeeRateDto(employeeRate);
    }

    public EmployeeRateDto getCurrentEmployeeRate() {
        var user = authService.getCurrentUser();
        if (!user.hasPermission(Permission.MANAGE_OWN_HOURLY_RATE)) {
            throw new PermissionDeniedException("You don't have permission to get own hourly rate");
        }

        var employeeRate = getEffectiveRate(user.getId()).orElse(null);

        if (employeeRate == null) {
            throw new EmployeeRateNotFoundException();
        }

        return attendanceMapper.toEmployeeRateDto(employeeRate);
    }

    @Transactional
    public ActiveSessionResponse clockIn(Long projectId, Long labelId, String description) {
        var user = authService.getCurrentUser();
        var project = getActiveProject(projectId, user.getId());
        if (hasActiveSessionAndAutoCancel(user)) {
            throw new ActiveSessionExistException();
        }

        var session = AttendanceSession.createClockInSession(user, project);
        var workDate = session.getWorkDate();
        var year = workDate.getYear();
        var month = (short) workDate.getMonthValue();

        var workSummary = workSummaryRepository
                .findProjectWorkSummary(user.getId(), projectId, year, month)
                .orElse(null);
        if (workSummary != null && workSummary.getStatus() != SummaryStatus.DRAFT) {
            throw new WorkSummaryHasBeenConfirmedException();
        }

        updateSession(labelId, description, session);
        attendanceSessionRepository.save(session);

        return getActiveSessionResponse(session, user);
    }

    @Transactional
    public ActiveSessionResponse clockOut(Long projectId, Long labelId, String description) {
        var user = authService.getCurrentUser();
        getProject(projectId, user.getId());
        var session = getProjectActiveSession(user.getId(), projectId);
        if (session == null) {
            throw new ActiveSessionNotFoundException();
        }

        AttendanceSession.updateClockOutSession(session);
        updateSession(labelId, description, session);
        findOrCreateWorkSummary(user, session);

        return getActiveSessionResponse(session, user);
    }

    private void findOrCreateWorkSummary(User user, AttendanceSession session) {
        var userId = user.getId();
        var project = session.getProject();
        var year = session.getWorkDate().getYear();
        var month = (short) session.getWorkDate().getMonthValue();

        var workSummary = workSummaryRepository
                .findProjectWorkSummary(userId, project.getId(), year, month)
                .orElse(null);
        if (workSummary == null) {
            var newWorkSummary = new WorkSummary();
            newWorkSummary.setStatus(SummaryStatus.DRAFT);
            newWorkSummary.setUser(user);
            newWorkSummary.setProject(project);
            newWorkSummary.setYear(year);
            newWorkSummary.setMonth(month);

            user.addWorkSummary(newWorkSummary);
            workSummaryRepository.save(newWorkSummary);
            return;
        }

        if (workSummary.getStatus() != SummaryStatus.DRAFT) {
            throw new DraftWorkSummaryNotFoundException();
        }
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

        var label = attendanceLabelRepository
                .getExistProjectLabel(session.getProject().getId(), labelId)
                .orElseThrow(LabelNotFoundException::new);
        session.setLabel(label);
    }

    public WorkSummaryDto getWorkSummary(Integer year, Short month, Long projectId) {
        var userId = AuthService.getCurrentUserId();
        getProject(projectId, userId);

        return workSummaryRepository.findProjectWorkSummary(userId, projectId, year, month)
                .map(attendanceMapper::toWorkSummaryDto)
                .orElseThrow(WorkSummaryNotFoundException::new);
    }

    private TrialSummaryDto getTrialSummary(Integer year, Short month, Long userId, Long projectId) {
        var startDate = LocalDate.of(year, month, 1);
        var endDate = startDate.plusMonths(1);

        var sessions = projectId == null
                ? attendanceSessionRepository.getSessionsForPeriod(userId, startDate, endDate)
                : attendanceSessionRepository.getUserProjectSessionsForPeriod(userId, projectId, startDate, endDate);
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

    public TrialSummaryDto previewWorkSummary(Integer year, Short month, Long userId, Long projectId) {
        var currentUser = authService.getCurrentUser();
        if (!currentUser.hasPermission(Permission.PREVIEW_OWN_WORK_SUMMARY)) {
            throw new PermissionDeniedException("You don't have permission to preview work summary");
        }

        if (userId == null) {
            userId = currentUser.getId();
        } else {
            var isTheSameUser = currentUser.getId().equals(userId);
            if (!isTheSameUser && !currentUser.hasPermission(Permission.PREVIEW_ALL_WORK_SUMMARY)) {
                throw new PermissionDeniedException("You don't have permission to preview work summary of other user");
            }
        }

        if (projectId != null) {
            getProject(projectId, userId);
        }

        return getTrialSummary(year, month, userId, projectId);
    }

    private void updateWorkSummary(WorkSummary summary, SummaryStatus summaryStatus) {
        var year = summary.getYear();
        var month = summary.getMonth();
        var trialSummary = getTrialSummary(year, month, summary.getUser().getId(), summary.getProject().getId());

        trialSummary.setId(summary.getId());
        if (trialSummary.hasActiveSessions()) {
            throw new ActiveSessionExistException();
        }

        summary.setHourlyRate(trialSummary.getHourlyRate());
        summary.setTotalMinutes(trialSummary.getTotalMinutes());
        summary.setSalaryAmount(trialSummary.getSalaryAmount());
        summary.setStatus(summaryStatus);
        workSummaryRepository.save(summary);
    }

    public WorkSummaryDto confirmWorkSummary(Long summaryId) {
        var currentUser = authService.getCurrentUser();
        if (!currentUser.hasPermission(Permission.CONFIRM_OWN_WORK_SUMMARY)) {
            throw new PermissionDeniedException("You don't have permission to confirm own work summary");
        }

        var summary = workSummaryRepository.findByIdAndStatus(summaryId, SummaryStatus.DRAFT).orElse(null);
        if (summary == null) {
            throw new DraftWorkSummaryNotFoundException();
        }

        var isTheSameUser = summary.getUser().getId().equals(currentUser.getId());
        if (!isTheSameUser && !currentUser.hasPermission(Permission.CONFIRM_ALL_WORK_SUMMARY)) {
            throw new PermissionDeniedException("You don't have permission to confirm work summary of other user");
        }

        updateWorkSummary(summary, SummaryStatus.CONFIRMED);

        return attendanceMapper.toWorkSummaryDto(summary);
    }

    public WorkSummaryDto payWorkSummary(Long summaryId) {
        var currentUser = authService.getCurrentUser();
        if (!currentUser.hasPermission(Permission.PAY_ALL_WORK_SUMMARY)) {
            throw new PermissionDeniedException("You don't have permission to pay work summary");
        }

        var summary = workSummaryRepository.findByIdAndStatus(summaryId, SummaryStatus.CONFIRMED).orElse(null);
        if (summary == null) {
            throw new DraftWorkSummaryNotFoundException();
        }

        summary.setStatus(SummaryStatus.PAID);
        workSummaryRepository.save(summary);

        return attendanceMapper.toWorkSummaryDto(summary);
    }

    public List<LabelDto> getLabels(Long projectId) {
        var userId = AuthService.getCurrentUserId();
        getProject(projectId, userId);

        return attendanceLabelRepository.getExistLabels(projectId)
                .stream().map(attendanceMapper::toLabelDto).toList();
    }

    public LabelDto createLabel(Long projectId, String name, String color) {
        var user = authService.getCurrentUser();
        var project = getActiveProject(projectId, user.getId());
        var hasExistName = attendanceLabelRepository.existsByName(projectId, name);
        if (hasExistName) {
            throw new LabelNameAlreadyExistException();
        }

        var maxSortOrder = attendanceLabelRepository.findMaxSortOrder(projectId);
        var nextSortOrder = maxSortOrder == null ? 0 : maxSortOrder + 1;

        var label = new AttendanceLabel();
        label.setName(name);
        label.setColor(color);
        label.setType(LabelType.WORK);
        label.setSortOrder(nextSortOrder);
        label.setProject(project);

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
            var hasExistName = attendanceLabelRepository.existsByName(label.getProject().getId(), name, id);
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

        var remainingLabels = attendanceLabelRepository.getExistLabels(label.getProject().getId());
        for (int i = 0; i < remainingLabels.size(); i++) {
            remainingLabels.get(i).setSortOrder(i);
        }
    }

    @Transactional
    public void reorderLabels(Long projectId, List<Long> ids) {
        var userId = AuthService.getCurrentUserId();
        getProject(projectId, userId);
        List<AttendanceLabel> labels = attendanceLabelRepository.findAllById(ids);

        // Safety check: ensure all belong to the project
        for (AttendanceLabel l : labels) {
            if (!l.getProject().getId().equals(projectId) || l.getDeletedAt() != null) {
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

    public void deleteSession(Long id) {
        var userId = AuthService.getCurrentUserId();
        var session = attendanceSessionRepository.findById(id)
                .filter(s -> s.getUser().getId().equals(userId))
                .orElseThrow(SessionNotFoundException::new);

        attendanceSessionRepository.delete(session);
    }

    @Transactional
    public SessionDto createSession(Long projectId, CreateSessionRequest request) {
        if (request.getClockOut().isBefore(request.getClockIn())) {
            throw new IllegalArgumentException("Clock out must be after clock in");
        }

        var user = authService.getCurrentUser();
        var project = getActiveProject(projectId, user.getId());
        var session = AttendanceSession.createSession(user, project, request);

        if (request.getLabelId() != null) {
            updateSessionLabel(request.getLabelId(), session);
        }

        if (request.getDescription() != null) {
            session.setDescription(request.getDescription());
        }

        findOrCreateWorkSummary(user, session);
        attendanceSessionRepository.save(session);

        return attendanceMapper.toDto(session);
    }

    public Page<WorkSummaryDto> getWorkSummaries(int page, int size, Long projectId) {
        var userId = AuthService.getCurrentUserId();
        var pageable = PageRequest.of(page, size);
        Page<WorkSummary> workSummaries;
        if (projectId == null) {
            workSummaries = workSummaryRepository.findWorkSummariesPaged(userId, pageable);
        } else {
            getProject(projectId, userId);
            workSummaries = workSummaryRepository.findProjectWorkSummariesPaged(userId, projectId, pageable);
        }

        return workSummaries.map(attendanceMapper::toWorkSummaryDto);
    }

    public List<WorkSummaryOption> getWorkSummaryOptions(Long projectId) {
        var userId = AuthService.getCurrentUserId();
        List<WorkSummary> workSummaries;
        if (projectId == null) {
            workSummaries = workSummaryRepository.findWorkSummaryOptions(userId);
        } else {
            getProject(projectId, userId);
            workSummaries = workSummaryRepository.findProjectWorkSummaryOptions(userId, projectId);
        }
        var options = workSummaries.stream()
                .map(attendanceMapper::toWorkSummaryOption)
                .collect(Collectors.toList());

        return options;
    }
}
