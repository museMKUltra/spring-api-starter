package com.codewithmosh.store.attendance;

public class LabelNameAlreadyExistException extends RuntimeException {
    public LabelNameAlreadyExistException() {
        super("Label name already exist");
    }
}
