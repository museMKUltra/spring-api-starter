package com.codewithmosh.store.attendance;

import lombok.Data;

import java.util.List;

@Data
public class ActiveSessionResponse {
    private boolean active;
    private SessionDto session;
    private List<SessionDto> sessions;
    private TrialSummaryDto summary;
}
