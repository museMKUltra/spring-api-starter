package com.codewithmosh.store.projects;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByUserIdAndName(Long userId, String name);

    Optional<Project> findByIdAndUserId(Long id, Long userId);
}
