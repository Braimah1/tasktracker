package com.tasktracker.controller;

import com.tasktracker.entity.User;
import com.tasktracker.security.SecurityUtils;
import com.tasktracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    @PostMapping("/create")
    public String createCategory(@RequestParam String name,
                                 RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser();
        try {
            categoryService.createCategory(name, user);
            redirectAttributes.addFlashAttribute("message", "Category \"" + name + "\" created.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser();
        try {
            categoryService.deleteCategory(id, user);
            redirectAttributes.addFlashAttribute("message", "Category deleted.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks";
    }
}
