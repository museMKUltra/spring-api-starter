package com.codewithmosh.store.attendance;

import com.codewithmosh.store.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Entity
@Table(name = "attendance_session", schema = "store_api")
public class AttendanceSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "label_id")
    private AttendanceLabel label;

    @Column(name = "clock_in")
    private Instant clockIn;

    @Column(name = "clock_out")
    private Instant clockOut;

    @Column(name = "work_minutes")
    private Long workMinutes;

    @Column(name = "work_date")
    private LocalDate workDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    private static Instant getClockTime() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public static AttendanceSession createClockInSession(User user) {
        var clockTime = getClockTime();
        var session = new AttendanceSession();
        session.setUser(user);
        session.setClockIn(clockTime);
        session.setWorkDate(new AttendanceTime(clockTime).getDateInZone());
        session.setStatus(SessionStatus.ACTIVE);

        return session;
    }

    public static AttendanceSession updateClockOutSession(AttendanceSession session) {
        var clockTime = getClockTime();
        var workMinutes = Duration.between(session.getClockIn(), clockTime).toMinutes();
        session.setClockOut(clockTime);
        session.setStatus(SessionStatus.COMPLETED);
        session.setWorkMinutes(workMinutes);

        return session;
    }

    public void setLabel(AttendanceLabel label) {
        this.label = label;
        label.getAttendanceSessions().add(this);
    }
}