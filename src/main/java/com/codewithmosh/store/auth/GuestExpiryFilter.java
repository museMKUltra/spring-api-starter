package com.codewithmosh.store.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class GuestExpiryFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!(auth.getDetails() instanceof Jwt jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println(jwt.isGuest());
        System.out.println(jwt.getExpiresAt());

        if (jwt.isGuestExpired()) {
            System.out.println(jwt.getUserId() + " guest account expired");
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Guest account expired");
            return;
        }

        filterChain.doFilter(request, response);
    }
}