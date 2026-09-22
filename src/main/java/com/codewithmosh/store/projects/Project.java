package com.codewithmosh.store.projects;

import com.codewithmosh.store.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Entity
@Table(name = "projects", schema = "store_api")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public static Project create(User user, String name, String description) {
        var project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.SECONDS));

        user.addProject(project);

        return project;
    }

    public void archive() {
        status = ProjectStatus.ARCHIVED;
    }
}
