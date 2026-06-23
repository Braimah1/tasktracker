package com.tasktracker.controller;

import com.tasktracker.entity.Task;
import com.tasktracker.entity.User;
import com.tasktracker.security.SecurityUtils;
import com.tasktracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TaskService taskService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String dashboard(Model model) {
        User user = securityUtils.getCurrentUser();

        taskService.syncOverdueStatuses(user);

        model.addAttribute("stats", taskService.getDashboardStats(user));
        model.addAttribute("recentTasks", taskService.getRecentTasks(user));
        model.addAttribute("user", user);

        return "dashboard";
    }
}
