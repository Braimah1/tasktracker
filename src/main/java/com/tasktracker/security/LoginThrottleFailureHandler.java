package com.tasktracker.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Tracks failed login attempts per IP and redirects with a distinct
 * "too many attempts" message once the threshold is hit, on top of
 * Spring Security's normal failure redirect.
 */
@Component
public class LoginThrottleFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final RateLimiter rateLimiter;

    public LoginThrottleFailureHandler(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        setDefaultFailureUrl("/auth/login?error=true");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException {
        String ip = clientIp(request);
        boolean withinLimit = rateLimiter.allow("login:" + ip, 10);

        if (!withinLimit) {
            getRedirectStrategy().sendRedirect(request, response, "/auth/login?throttled=true");
            return;
        }

        super.onAuthenticationFailure(request, response, exception);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
