package com.tasktracker.service;

import com.tasktracker.dto.PasswordDtos;
import com.tasktracker.dto.RegisterRequest;
import com.tasktracker.entity.Category;
import com.tasktracker.entity.User;
import com.tasktracker.repository.CategoryRepository;
import com.tasktracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.token-expiry-hours:24}")
    private int tokenExpiryHours;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        if (!request.passwordsMatch()) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = User.builder()
            .firstName(request.getFirstName().trim())
            .lastName(request.getLastName().trim())
            .email(request.getEmail().toLowerCase().trim())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

        user = userRepository.save(user);

        // Seed default categories
        List<String> defaults = List.of("Work", "Personal", "School", "Fitness", "Finance");
        for (String name : defaults) {
            categoryRepository.save(Category.builder().name(name).user(user).build());
        }

        log.info("New user registered: {}", user.getEmail());
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("No account found for that email address."));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(tokenExpiryHours));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), token);

        return token;
    }

    @Transactional
    public void resetPassword(PasswordDtos.ResetPasswordRequest request) {
        if (!request.passwordsMatch()) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = userRepository.findByPasswordResetToken(request.getToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link."));

        if (user.getPasswordResetTokenExpiry() == null ||
            LocalDateTime.now().isAfter(user.getPasswordResetTokenExpiry())) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset successfully for: {}", user.getEmail());
    }

    /**
     * Permanently deletes a user account along with all associated data.
     * Tasks and categories are removed automatically via JPA cascade
     * (User.tasks / User.categories are CascadeType.ALL) and the
     * corresponding ON DELETE CASCADE foreign keys in the schema —
     * no manual cleanup of child records is needed here.
     *
     * @param user the account to delete
     * @param currentPassword the password the user typed to confirm deletion
     */
    @Transactional
    public void deleteAccount(User user, String currentPassword) {
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password. Account was not deleted.");
        }

        String email = user.getEmail();
        userRepository.delete(user);

        log.info("Account permanently deleted: {}", email);
    }
}
