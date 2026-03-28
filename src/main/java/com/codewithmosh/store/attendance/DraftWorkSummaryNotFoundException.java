package com.codewithmosh.store.attendance;

public class DraftWorkSummaryNotFoundException extends RuntimeException {
    public DraftWorkSummaryNotFoundException() {
        super("Draft work summary not found");
    }
}
