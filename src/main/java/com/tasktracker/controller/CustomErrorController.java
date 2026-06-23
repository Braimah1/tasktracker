package com.tasktracker.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Replaces Spring Boot's default Whitelabel Error Page (used for 404s,
 * unmapped routes, and errors that occur before reaching a controller)
 * with our own styled error page.
 */
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = (status != null) ? Integer.parseInt(status.toString()) : 500;

        String message = switch (statusCode) {
            case 404 -> "We couldn't find the page you were looking for.";
            case 403 -> "You don't have permission to access that.";
            default -> "Something went wrong on our end. Please try again.";
        };

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", message);
        return "error/generic";
    }
}
