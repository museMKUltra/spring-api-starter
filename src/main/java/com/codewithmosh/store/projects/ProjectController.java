package com.codewithmosh.store.projects;

import com.codewithmosh.store.common.ErrorDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@AllArgsConstructor
@RestController
@RequestMapping("/projects")
class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var project = projectService.createProject(request.getName(), request.getDescription());
        var uri = uriBuilder.path("/api/projects/{id}").buildAndExpand(project.getId()).toUri();

        return ResponseEntity.created(uri).body(project);
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ProjectDto> archiveProject(@PathVariable Long id) {
        var project = projectService.archiveProject(id);

        return ResponseEntity.ok(project);
    }

    @ExceptionHandler({ProjectNotFoundException.class, ProjectNameAlreadyExistException.class})
    public ResponseEntity<ErrorDto> handleBadRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(exception.getMessage()));
    }
}
