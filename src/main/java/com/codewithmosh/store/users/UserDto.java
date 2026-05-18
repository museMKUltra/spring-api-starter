package com.codewithmosh.store.users;

import com.codewithmosh.store.attendance.AttendanceTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;

@AllArgsConstructor
@Getter
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private boolean guest;
    private Instant expiresAt;
    private Role role;

    public String getExpiresAt() {
        return expiresAt != null ? new AttendanceTime(expiresAt).getDateTimeInZone() : null;
    }

    public Set<Permission> getPermissions() {
        return role.getPermissions();
    }
}
