package com.codewithmosh.store.attendance;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateSessionRequest {
    private Instant clockIn;
    private Instant clockOut;
    private Long labelId;
    @Size(max = 255, message = "Description must be less than 255 characters.")
    private String description;
}
