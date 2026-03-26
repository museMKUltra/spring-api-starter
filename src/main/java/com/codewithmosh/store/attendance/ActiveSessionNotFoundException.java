package com.codewithmosh.store.attendance;

public class ActiveSessionNotFoundException extends RuntimeException {
    public ActiveSessionNotFoundException() {
        super("Active session not found");
    }
}
