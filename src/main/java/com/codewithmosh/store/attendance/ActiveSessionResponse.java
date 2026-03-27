package com.codewithmosh.store.attendance;

import com.codewithmosh.store.users.SummaryDto;
import lombok.Data;

@Data
public class ActiveSessionResponse {
    private boolean active;
    private SessionDto session;
    private SummaryDto summary;
}
