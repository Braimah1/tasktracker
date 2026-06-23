package com.tasktracker.controller;

import com.tasktracker.dto.TaskRequest;
import com.tasktracker.entity.Task;
import com.tasktracker.entity.User;
import com.tasktracker.security.SecurityUtils;
import com.tasktracker.service.CategoryService;
import com.tasktracker.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listTasks(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String priority,
                            @RequestParam(required = false) Long categoryId,
                            Model model) {
        User user = securityUtils.getCurrentUser();
        taskService.syncOverdueStatuses(user);

        model.addAttribute("tasks", taskService.getFilteredTasks(user, search, status, priority, categoryId));
        model.addAttribute("categories", categoryService.getUserCategories(user));
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("priorities", Task.Priority.values());
        model.addAttribute("statuses", Task.Status.values());
        return "tasks/list";
    }

    @GetMapping("/create")
    public String createTaskForm(Model model) {
        User user = securityUtils.getCurrentUser();
        model.addAttribute("taskRequest", new TaskRequest());
        model.addAttribute("categories", categoryService.getUserCategories(user));
        model.addAttribute("priorities", Task.Priority.values());
        return "tasks/create";
    }

    @PostMapping("/create")
    public String createTask(@Valid @ModelAttribute("taskRequest") TaskRequest request,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        User user = securityUtils.getCurrentUser();

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getUserCategories(user));
            model.addAttribute("priorities", Task.Priority.values());
            return "tasks/create";
        }

        try {
            taskService.createTask(request, user);
            redirectAttributes.addFlashAttribute("message", "Task created successfully.");
            return "redirect:/tasks";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.getUserCategories(user));
            model.addAttribute("priorities", Task.Priority.values());
            return "tasks/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editTaskForm(@PathVariable Long id, Model model) {
        User user = securityUtils.getCurrentUser();
        Task task = taskService.getTaskById(id, user);

        TaskRequest request = new TaskRequest();
        request.setTitle(task.getTitle());
        request.setDescription(task.getDescription());
        request.setPriority(task.getPriority());
        request.setDueDate(task.getDueDate());
        request.setDueTime(task.getDueTime());
        if (task.getCategory() != null) request.setCategoryId(task.getCategory().getId());

        model.addAttribute("taskRequest", request);
        model.addAttribute("task", task);
        model.addAttribute("categories", categoryService.getUserCategories(user));
        model.addAttribute("priorities", Task.Priority.values());
        return "tasks/edit";
    }

    @PostMapping("/edit/{id}")
    public String editTask(@PathVariable Long id,
                           @Valid @ModelAttribute("taskRequest") TaskRequest request,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        User user = securityUtils.getCurrentUser();

        if (result.hasErrors()) {
            model.addAttribute("task", taskService.getTaskById(id, user));
            model.addAttribute("categories", categoryService.getUserCategories(user));
            model.addAttribute("priorities", Task.Priority.values());
            return "tasks/edit";
        }

        try {
            taskService.updateTask(id, request, user);
            redirectAttributes.addFlashAttribute("message", "Task updated successfully.");
            return "redirect:/tasks";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("task", taskService.getTaskById(id, user));
            model.addAttribute("categories", categoryService.getUserCategories(user));
            model.addAttribute("priorities", Task.Priority.values());
            return "tasks/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser();
        try {
            taskService.deleteTask(id, user);
            redirectAttributes.addFlashAttribute("message", "Task deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }

    @PostMapping("/toggle/{id}")
    public String toggleTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser();
        try {
            Task task = taskService.toggleComplete(id, user);
            String msg = task.getStatus() == Task.Status.COMPLETED ? "Task marked complete!" : "Task marked pending.";
            redirectAttributes.addFlashAttribute("message", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }
}
