package com.codewithmosh.store.attendance;

public class ActiveSessionExistException extends RuntimeException {
    public ActiveSessionExistException() {
        super("Active session exist");
    }
}
