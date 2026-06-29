package com.tasktracker.service;

import com.tasktracker.dto.DashboardStats;
import com.tasktracker.dto.TaskRequest;
import com.tasktracker.entity.Category;
import com.tasktracker.entity.Task;
import com.tasktracker.entity.Task.Status;
import com.tasktracker.entity.User;
import com.tasktracker.repository.CategoryRepository;
import com.tasktracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Task createTask(TaskRequest request, User user) {
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndUser(request.getCategoryId(), user).orElse(null);
        }

        Task task = Task.builder()
            .title(request.getTitle().trim())
            .description(request.getDescription())
            .priority(request.getPriority())
            .dueDate(request.getDueDate())
            .dueTime(request.getDueTime())
            .user(user)
            .category(category)
            .build();

        task.setStatus(calculateStatus(task));

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long id, TaskRequest request, User user) {
        Task task = taskRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new IllegalArgumentException("Task not found."));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndUser(request.getCategoryId(), user).orElse(null);
        }

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setDueTime(request.getDueTime());
        task.setCategory(category);

        // Recalculate status whenever a task is edited — e.g. pushing the
        // due date into the past should mark it Overdue immediately, and
        // pulling an Overdue task's due date into the future should clear
        // that status, without ever overriding a manually Completed task.
        task.setStatus(calculateStatus(task));

        return taskRepository.save(task);
    }

    /**
     * Single source of truth for what a task's status should be, given
     * its current due date and status. Called from both createTask()
     * and updateTask() so status is always recalculated consistently
     * whenever a task's data changes, rather than only when
     * syncOverdueStatuses() happens to run separately.
     *
     * Rules:
     *  - Completed tasks are never changed by this method.
     *  - A past due date (and not completed) → Overdue.
     *  - Otherwise, an existing in-progress task stays In Progress.
     *  - Anything else (future due date, today, or no due date) → Pending.
     */
    private Status calculateStatus(Task task) {
        if (task.getStatus() == Status.COMPLETED) {
            return Status.COMPLETED;
        }

        if (task.getDueDate() != null && LocalDate.now().isAfter(task.getDueDate())) {
            return Status.OVERDUE;
        }

        if (task.getStatus() == Status.IN_PROGRESS) {
            return Status.IN_PROGRESS;
        }

        return Status.PENDING;
    }

    @Transactional
    public void deleteTask(Long id, User user) {
        Task task = taskRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new IllegalArgumentException("Task not found."));
        taskRepository.delete(task);
    }

    @Transactional
    public Task toggleComplete(Long id, User user) {
        Task task = taskRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new IllegalArgumentException("Task not found."));

        if (task.getStatus() == Status.COMPLETED) {
            task.setStatus(Status.PENDING);
            task.setCompletedAt(null);
        } else {
            task.setStatus(Status.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
        }

        return taskRepository.save(task);
    }

    @Transactional
    public void syncOverdueStatuses(User user) {
        List<Task> overdue = taskRepository.findOverdueTasks(user, LocalDate.now());
        for (Task t : overdue) {
            if (t.getStatus() != Status.OVERDUE) {
                t.setStatus(Status.OVERDUE);
                taskRepository.save(t);
            }
        }
    }

    public List<Task> getFilteredTasks(User user, String search, String status, String priority, Long categoryId) {
        Status statusEnum = null;
        Task.Priority priorityEnum = null;

        try { if (status != null && !status.isBlank()) statusEnum = Status.valueOf(status); } catch (Exception ignored) {}
        try { if (priority != null && !priority.isBlank()) priorityEnum = Task.Priority.valueOf(priority); } catch (Exception ignored) {}

        String searchTerm = (search != null && !search.isBlank()) ? search : null;

        return taskRepository.findWithFilters(user, searchTerm, statusEnum, priorityEnum, categoryId);
    }

    public Task getTaskById(Long id, User user) {
        return taskRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new IllegalArgumentException("Task not found."));
    }

    public DashboardStats getDashboardStats(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekEnd = today.plusDays(7);

        long total = taskRepository.countByUser(user);
        long completed = taskRepository.countByUserAndStatus(user, Status.COMPLETED);
        long overdue = taskRepository.countOverdueTasks(user, today);
        long dueToday = taskRepository.countTasksDueToday(user, today);
        long dueThisWeek = taskRepository.countTasksDueThisWeek(user, today, weekEnd);
        long pending = total - completed - overdue;
        if (pending < 0) pending = 0;

        double completionPct = total > 0 ? Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0.0;

        return DashboardStats.builder()
            .totalTasks(total)
            .completedTasks(completed)
            .pendingTasks(pending)
            .overdueTasks(overdue)
            .tasksDueToday(dueToday)
            .tasksDueThisWeek(dueThisWeek)
            .completionPercentage(completionPct)
            .build();
    }

    public List<Task> getRecentTasks(User user) {
        List<Task> all = taskRepository.findByUserOrderByCreatedAtDesc(user);
        return all.size() > 5 ? all.subList(0, 5) : all;
    }
}
