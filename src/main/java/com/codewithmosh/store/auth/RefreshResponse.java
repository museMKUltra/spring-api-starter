package com.codewithmosh.store.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RefreshResponse {
    private Jwt accessToken;
    private Jwt refreshToken;
}
