package com.codewithmosh.store.attendance;

import com.codewithmosh.store.projects.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "attendance_label", schema = "store_api")
public class AttendanceLabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private LabelType type;

    @Column(name = "color")
    private String color;

    @OneToMany(mappedBy = "label")
    private Set<AttendanceSession> attendanceSessions = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public boolean isGlobal() {
        return project == null;
    }
}