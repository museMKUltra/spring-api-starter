package com.codewithmosh.store.attendance;

public class NotDraftWorkSummaryException extends RuntimeException {
    public NotDraftWorkSummaryException() {
        super("Work summary is not draft");
    }
}
