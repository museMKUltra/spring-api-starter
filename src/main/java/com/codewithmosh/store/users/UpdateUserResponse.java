package com.codewithmosh.store.users;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUserResponse {
    private UserDto user;
    private String token;
}
