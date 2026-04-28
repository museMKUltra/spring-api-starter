package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RefreshResponse {
    private Jwt accessToken;
    private Jwt refreshToken;
    private UserDto user;
}
