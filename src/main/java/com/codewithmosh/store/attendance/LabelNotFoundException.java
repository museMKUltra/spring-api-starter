package com.codewithmosh.store.attendance;

public class LabelNotFoundException extends RuntimeException {
    public LabelNotFoundException() {
        super("Label not found");
    }
}
