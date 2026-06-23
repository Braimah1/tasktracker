package com.tasktracker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class DashboardStats {
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long overdueTasks;
    private long tasksDueToday;
    private long tasksDueThisWeek;
    private double completionPercentage;
}
