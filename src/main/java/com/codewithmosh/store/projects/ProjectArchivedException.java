package com.codewithmosh.store.projects;

public class ProjectArchivedException extends RuntimeException {
    public ProjectArchivedException() {
        super("Project has been archived");
    }
}
