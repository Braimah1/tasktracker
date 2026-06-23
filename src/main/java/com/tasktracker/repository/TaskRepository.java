package com.tasktracker.repository;

import com.tasktracker.entity.Task;
import com.tasktracker.entity.Task.Priority;
import com.tasktracker.entity.Task.Status;
import com.tasktracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserOrderByCreatedAtDesc(User user);

    Optional<Task> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    long countByUserAndStatus(User user, Status status);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.status != 'COMPLETED' AND t.dueDate < :today")
    List<Task> findOverdueTasks(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.status != 'COMPLETED' AND t.dueDate < :today")
    long countOverdueTasks(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.dueDate = :today AND t.status != 'COMPLETED'")
    List<Task> findTasksDueToday(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.dueDate = :today AND t.status != 'COMPLETED'")
    long countTasksDueToday(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.dueDate BETWEEN :start AND :end AND t.status != 'COMPLETED'")
    List<Task> findTasksDueThisWeek(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.dueDate BETWEEN :start AND :end AND t.status != 'COMPLETED'")
    long countTasksDueThisWeek(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT t FROM Task t WHERE t.user = :user " +
           "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "ORDER BY t.createdAt DESC")
    List<Task> findWithFilters(@Param("user") User user,
                                @Param("search") String search,
                                @Param("status") Status status,
                                @Param("priority") Priority priority,
                                @Param("categoryId") Long categoryId);
}
