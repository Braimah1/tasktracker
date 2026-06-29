package com.tasktracker.controller;

import com.tasktracker.entity.User;
import com.tasktracker.security.SecurityUtils;
import com.tasktracker.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String accountPage(Model model) {
        User user = securityUtils.getCurrentUser();
        model.addAttribute("user", user);
        return "account";
    }

    @PostMapping("/delete")
    public String deleteAccount(@RequestParam String currentPassword,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser();

        try {
            userService.deleteAccount(user, currentPassword);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/account";
        }

        // Account is gone — invalidate the session and clear the auth
        // cookie the same way the normal /auth/logout flow does (see
        // SecurityConfig), so no stale authenticated session or cookie
        // can remain for a now-deleted user.
        new SecurityContextLogoutHandler().logout(request, response, null);

        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        response.addCookie(sessionCookie);

        redirectAttributes.addFlashAttribute("message",
            "Your account and all associated data have been permanently deleted.");
        return "redirect:/auth/login";
    }
}
