package com.codewithmosh.store.attendance;

public class WorkSummaryHasBeenConfirmedException extends RuntimeException {
    public WorkSummaryHasBeenConfirmedException() {
        super("Work summary has been confirmed");
    }
}
