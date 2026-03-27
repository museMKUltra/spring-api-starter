package com.codewithmosh.store.attendance;

public class WorkSummaryNotFoundException extends RuntimeException {
    WorkSummaryNotFoundException() {
        super("Work summary not found");
    }
}
