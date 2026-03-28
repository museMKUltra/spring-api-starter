package com.codewithmosh.store.attendance;

import lombok.Data;

@Data
public class ActiveSessionResponse {
    private boolean active;
    private SessionDto session;
    private TrialSummaryDto summary;
}
