package com.codewithmosh.store.users;

import java.util.Set;

public enum Role {
    USER(Set.of(
            Permission.PREVIEW_OWN_WORK_SUMMARY
    )),
    PREMIUM(Set.of(
            Permission.MANAGE_OWN_HOURLY_RATE,
            Permission.PREVIEW_OWN_WORK_SUMMARY,
            Permission.CONFIRM_OWN_WORK_SUMMARY
    )),
    ADMIN(Set.of(
            Permission.MANAGE_OWN_HOURLY_RATE,
            Permission.MANAGE_ALL_HOURLY_RATE,
            Permission.PREVIEW_OWN_WORK_SUMMARY,
            Permission.PREVIEW_ALL_WORK_SUMMARY,
            Permission.CONFIRM_OWN_WORK_SUMMARY,
            Permission.CONFIRM_ALL_WORK_SUMMARY,
            Permission.PAY_ALL_WORK_SUMMARY,
            Permission.MANAGE_USERS
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}