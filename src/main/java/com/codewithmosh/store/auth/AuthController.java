package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.MeDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final JwtConfig jwtConfig;
    private final AuthService authService;

    private static @NonNull Cookie getCookie(String refreshToken, int expiration) {
        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(expiration);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);

        return cookie;
    }

    private void setCookie(HttpServletResponse response, String refreshToken) {
        var cookie = getCookie(refreshToken, jwtConfig.getRefreshTokenExpiration());
        response.addCookie(cookie);
    }

    private void resetCookie(HttpServletResponse response, String refreshToken) {
        var cookie = getCookie(refreshToken, 0);
        response.addCookie(cookie);
    }

    @PostMapping("/login")
    public JwtResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        var loginResult = authService.login(request);
        var refreshToken = loginResult.getRefreshToken().toString();
        var accessToken = loginResult.getAccessToken().toString();

        setCookie(response, refreshToken);

        return new JwtResponse(accessToken);
    }

    @PostMapping("/refresh")
    public JwtResponse refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        var refreshResult = authService.refresh(refreshToken);
        var newRefreshToken = refreshResult.getRefreshToken().toString();
        var newAccessToken = refreshResult.getAccessToken().toString();

        setCookie(response, newRefreshToken);

        return new JwtResponse(newAccessToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        resetCookie(response, refreshToken);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeDto> me() {
        var meDto = authService.getMe();
        if (meDto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(meDto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
