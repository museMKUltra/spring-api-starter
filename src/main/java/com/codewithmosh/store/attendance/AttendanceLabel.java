package com.codewithmosh.store.attendance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
}