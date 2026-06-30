package com.tasktracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, OVERDUE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "due_time")
    private LocalTime dueTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Status.PENDING;
        }
    }

    public boolean isOverdue() {
        if (status == Status.COMPLETED)
            return false;
        if (dueDate == null)
            return false;
        return LocalDate.now().isAfter(dueDate);
    }

    public long getDaysRemaining() {
        if (dueDate == null)
            return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    /**
     * Returns a human-readable countdown that gets more precise as the
     * deadline approaches:
     *  - More than 1 day away  -> "5 days left" / "1 day left"
     *  - Due today             -> "12 hours left" / "1 hour left"
     *  - Less than 1 hour away -> "45 minutes left" / "1 minute left"
     *
     * Falls back to whole-day phrasing if no due time is set, since
     * there's no specific moment on the due date to count down to.
     * Returns null when there's nothing meaningful to display (no due
     * date, already overdue, or completed) — callers decide how to
     * render those states (e.g. "Done ✓", "—", "Xd overdue").
     */
    public String getTimeRemainingDisplay() {
        if (dueDate == null || status == Status.COMPLETED) {
            return null;
        }

        LocalDate today = LocalDate.now();
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);

        if (daysUntil > 1) {
            return daysUntil + " day" + (daysUntil == 1 ? "" : "s") + " left";
        }

        if (daysUntil < 0) {
            return null; // overdue — caller handles this case separately
        }

        if (daysUntil == 1) {
            return "1 day left";
        }

        // daysUntil == 0: due today. Without a specific due time, there's
        // no moment today to count down to, so keep whole-day phrasing.
        if (dueTime == null) {
            return "Due today";
        }

        LocalDateTime dueAt = LocalDateTime.of(dueDate, dueTime);
        LocalDateTime now = LocalDateTime.now();

        if (!now.isBefore(dueAt)) {
            // The due time has technically passed, but this app only
            // treats a task as Overdue once the calendar day has passed
            // (see TaskService.calculateStatus) — so on the due date
            // itself, floor at zero rather than showing nothing.
            return "0 minutes left";
        }

        long minutesUntil = java.time.temporal.ChronoUnit.MINUTES.between(now, dueAt);

        if (minutesUntil < 60) {
            return minutesUntil + " minute" + (minutesUntil == 1 ? "" : "s") + " left";
        }

        long hoursUntil = minutesUntil / 60;
        return hoursUntil + " hour" + (hoursUntil == 1 ? "" : "s") + " left";
    }
}
