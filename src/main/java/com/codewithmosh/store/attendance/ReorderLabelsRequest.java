package com.codewithmosh.store.attendance;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ReorderLabelsRequest {
    @NotEmpty(message = "IDs are required.")
    private Long[] ids;
}
