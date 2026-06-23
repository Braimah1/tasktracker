package com.tasktracker.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Catches anything that escapes individual controllers so we never
 * show a raw stack trace or Whitelabel error page to an end user.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        log.warn("Handled IllegalArgumentException: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 400);
        return "error/generic";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, Model model) {
        log.warn("Handled IllegalStateException: {}", ex.getMessage());
        model.addAttribute("errorMessage", "You need to be signed in to do that.");
        model.addAttribute("statusCode", 401);
        return "error/generic";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, Model model) {
        // Log full detail server-side only — never shown to the user.
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        log.error("Unhandled exception: {}\n{}", ex.getMessage(), sw);

        model.addAttribute("errorMessage", "Something went wrong on our end. Please try again.");
        model.addAttribute("statusCode", 500);
        return "error/generic";
    }
}
