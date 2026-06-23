package com.tasktracker.dto;

import com.tasktracker.entity.Task.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Title must be under 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must be under 5000 characters")
    private String description;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime dueTime;

    private Long categoryId;
}
