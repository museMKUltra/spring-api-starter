package com.codewithmosh.store.attendance;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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

    @Size(max = 100)
    @NotNull
    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private LabelType type;

    @Column(name = "color")
    private String color;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "label")
    private Set<AttendanceSession> attendanceSessions = new HashSet<>();
}