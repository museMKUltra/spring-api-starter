package com.codewithmosh.store.attendance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TrialSummaryLabelDto {
    private Long id;
    private String name;
    private String color;
    private Long workMinutes;
    @JsonProperty("isGlobal")
    private boolean isGlobal;
}
