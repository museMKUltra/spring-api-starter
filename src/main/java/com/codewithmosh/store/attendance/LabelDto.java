package com.codewithmosh.store.attendance;

import com.codewithmosh.store.users.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class LabelDto {
    private Long id;
    private String name;
    private String color;
    @Getter(AccessLevel.NONE)
    private User user;

    public boolean getIsGlobal() {
        return user == null;
    }
}
