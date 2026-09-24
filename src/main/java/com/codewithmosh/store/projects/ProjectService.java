package com.codewithmosh.store.projects;

import com.codewithmosh.store.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final AuthService authService;

    public List<ProjectDto> getProjects() {
        var userId = AuthService.getCurrentUserId();

        return projectRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(projectMapper::toDto)
                .toList();
    }

    public ProjectDto createProject(String name, String description) {
        var user = authService.getCurrentUser();
        var hasExistName = projectRepository.existsByUserIdAndName(user.getId(), name);
        if (hasExistName) {
            throw new ProjectNameAlreadyExistException();
        }

        var project = Project.create(user, name, description);
        projectRepository.save(project);

        return projectMapper.toDto(project);
    }

    public ProjectDto updateProject(Long id, String name, String description) {
        var userId = AuthService.getCurrentUserId();
        var project = projectRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(ProjectNotFoundException::new);

        var hasExistName = projectRepository.existsByUserIdAndNameAndIdNot(userId, name, id);
        if (hasExistName) {
            throw new ProjectNameAlreadyExistException();
        }

        project.update(name, description);
        projectRepository.save(project);

        return projectMapper.toDto(project);
    }

    public ProjectDto archiveProject(Long id) {
        var userId = AuthService.getCurrentUserId();
        var project = projectRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(ProjectNotFoundException::new);

        project.archive();
        projectRepository.save(project);

        return projectMapper.toDto(project);
    }

    public ProjectDto restoreProject(Long id) {
        var userId = AuthService.getCurrentUserId();
        var project = projectRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(ProjectNotFoundException::new);

        project.restore();
        projectRepository.save(project);

        return projectMapper.toDto(project);
    }
}
