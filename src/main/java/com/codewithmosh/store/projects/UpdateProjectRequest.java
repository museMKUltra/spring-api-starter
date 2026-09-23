package com.codewithmosh.store.projects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be equal and less than 100 characters.")
    private String name;

    @Size(max = 255, message = "Description must be equal and less than 255 characters.")
    private String description;
}
