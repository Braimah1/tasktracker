package com.tasktracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@tasktracker.app}")
    private String fromAddress;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends the password reset email. If email sending is disabled
     * (no MAIL_USERNAME configured), the reset link is logged instead
     * so the app remains fully functional in dev / before SMTP setup.
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = baseUrl + "/auth/reset-password?token=" + token;

        if (!mailEnabled) {
            log.info("[DEV MODE - email disabled] Password reset link for {}: {}", toEmail, resetLink);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Reset your TaskTracker password");
            message.setText(
                "Hello,\n\n" +
                "We received a request to reset your TaskTracker password.\n\n" +
                "Click the link below to choose a new password. This link expires in 24 hours.\n\n" +
                resetLink + "\n\n" +
                "If you didn't request this, you can safely ignore this email.\n\n" +
                "— TaskTracker"
            );
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            // Never let email failures break the request/leak internals to the user.
            // Fall back to logging the link so the flow is still usable.
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            log.info("[FALLBACK] Password reset link for {}: {}", toEmail, resetLink);
        }
    }
}
