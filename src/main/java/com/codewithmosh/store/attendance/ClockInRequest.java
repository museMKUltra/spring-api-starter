package com.codewithmosh.store.attendance;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClockInRequest {
    private Long labelId;

    @Size(max = 255, message = "Description must be less than 255 characters.")
    private String description;
}
