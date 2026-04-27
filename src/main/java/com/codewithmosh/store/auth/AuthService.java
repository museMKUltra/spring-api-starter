package com.codewithmosh.store.auth;

import com.codewithmosh.store.attendance.RefreshTokenService;
import com.codewithmosh.store.users.MeDto;
import com.codewithmosh.store.users.User;
import com.codewithmosh.store.users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public static Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        return (Long) authentication.getPrincipal();
    }

    private Jwt rotateRefreshToken(String refreshToken, User user) {
        refreshTokenService.delete(refreshToken);

        var newRefreshToken = jwtService.generateRefreshToken(user);
        refreshTokenService.create(user, newRefreshToken.toString());

        return newRefreshToken;
    }

    public User getCurrentUser() {
        return userRepository.findById(getCurrentUserId()).orElse(null);
    }

    public MeDto getMe() {
        return userRepository.findMe(getCurrentUserId()).orElse(null);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenService.create(user, refreshToken.toString());

        return new LoginResponse(accessToken, refreshToken);
    }

    public RefreshResponse refresh(String refreshToken) {
        var jwt = jwtService.parseToken(refreshToken);
        if (jwt == null || jwt.isExpired()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        var storedToken = refreshTokenService.verify(refreshToken);
        var user = storedToken.getUser();

        var newRefreshToken = rotateRefreshToken(refreshToken, user);
        var newAccessToken = jwtService.generateAccessToken(user);

        return new RefreshResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenService.delete(refreshToken);
    }
}
