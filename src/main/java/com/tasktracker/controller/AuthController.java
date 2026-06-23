package com.tasktracker.controller;

import com.tasktracker.dto.PasswordDtos;
import com.tasktracker.dto.RegisterRequest;
import com.tasktracker.security.RateLimiter;
import com.tasktracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final RateLimiter rateLimiter;

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String expired,
                            @RequestParam(required = false) String throttled,
                            Model model) {
        if (throttled != null) model.addAttribute("error", "Too many failed attempts. Please wait a few minutes and try again.");
        else if (error != null) model.addAttribute("error", "Invalid email or password.");
        if (logout != null) model.addAttribute("message", "You've been logged out successfully.");
        if (expired != null) model.addAttribute("error", "Your session has expired. Please log in again.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult result,
                           HttpServletRequest httpRequest,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (!rateLimiter.allow("register:" + clientIp(httpRequest), 10)) {
            model.addAttribute("error", "Too many attempts. Please try again in a few minutes.");
            return "auth/register";
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        if (!request.passwordsMatch()) {
            model.addAttribute("error", "Passwords do not match.");
            return "auth/register";
        }

        try {
            userService.register(request);
            redirectAttributes.addFlashAttribute("message", "Account created! Please log in.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 HttpServletRequest httpRequest,
                                 RedirectAttributes redirectAttributes) {
        if (rateLimiter.allow("forgot-password:" + clientIp(httpRequest), 5)) {
            try {
                userService.generatePasswordResetToken(email);
            } catch (IllegalArgumentException e) {
                // Swallow silently — don't reveal whether the email exists.
            }
        }
        // Always show the same message regardless of outcome or rate limit, for security.
        redirectAttributes.addFlashAttribute("message",
            "If an account exists for that email, a password reset link has been sent.");
        return "redirect:/auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("resetRequest", new PasswordDtos.ResetPasswordRequest());
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetRequest") PasswordDtos.ResetPasswordRequest request,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("token", request.getToken());
            return "auth/reset-password";
        }

        if (!request.passwordsMatch()) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", request.getToken());
            return "auth/reset-password";
        }

        try {
            userService.resetPassword(request);
            redirectAttributes.addFlashAttribute("message", "Password reset successfully. Please log in.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", request.getToken());
            return "auth/reset-password";
        }
    }
}
