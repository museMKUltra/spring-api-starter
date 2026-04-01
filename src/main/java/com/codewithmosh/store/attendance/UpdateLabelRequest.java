package com.codewithmosh.store.attendance;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLabelRequest {
    @Size(max = 100, message = "Name must be equal and less than 100 characters.")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code (e.g., #010101)")
    private String color;

    @AssertTrue(message = "At least one field (name or color) must be provided")
    private boolean isAtLeastOneFieldPresent() {
        return name != null || color != null;
    }
}
