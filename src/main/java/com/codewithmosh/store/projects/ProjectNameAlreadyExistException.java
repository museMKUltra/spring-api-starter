package com.codewithmosh.store.projects;

public class ProjectNameAlreadyExistException extends RuntimeException {
    public ProjectNameAlreadyExistException() {
        super("Project name already exist");
    }
}
