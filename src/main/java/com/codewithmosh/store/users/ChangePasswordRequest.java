package com.codewithmosh.store.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Password is required.")
    private String oldPassword;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 25, message = "Password must be between 8 and 25 characters long.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter and one number."
    )
    private String newPassword;
}
