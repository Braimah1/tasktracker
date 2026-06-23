-- =============================================
-- TASKTRACKER — DATABASE SETUP SCRIPT
-- Run this before starting the application
-- =============================================

-- Create database
CREATE DATABASE IF NOT EXISTS tasktracker
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE tasktracker;

-- Drop tables if re-running (order matters due to FKs)
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- =============================================
-- USERS TABLE
-- =============================================
CREATE TABLE users (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name                  VARCHAR(100) NOT NULL,
    last_name                   VARCHAR(100) NOT NULL,
    email                       VARCHAR(255) NOT NULL UNIQUE,
    password                    VARCHAR(255) NOT NULL,
    created_at                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    password_reset_token        VARCHAR(255),
    password_reset_token_expiry DATETIME,

    INDEX idx_users_email (email),
    INDEX idx_users_reset_token (password_reset_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- CATEGORIES TABLE
-- =============================================
CREATE TABLE categories (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    user_id BIGINT       NOT NULL,

    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_categories_user (user_id),
    UNIQUE KEY uq_category_name_user (name, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- TASKS TABLE
-- =============================================
CREATE TABLE tasks (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255)                                          NOT NULL,
    description  TEXT,
    priority     ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')            NOT NULL DEFAULT 'MEDIUM',
    status       ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE') NOT NULL DEFAULT 'PENDING',
    due_date     DATE,
    due_time     TIME,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,
    user_id      BIGINT NOT NULL,
    category_id  BIGINT,

    CONSTRAINT fk_tasks_user     FOREIGN KEY (user_id)     REFERENCES users (id)      ON DELETE CASCADE,
    CONSTRAINT fk_tasks_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,

    INDEX idx_tasks_user        (user_id),
    INDEX idx_tasks_status      (status),
    INDEX idx_tasks_due_date    (due_date),
    INDEX idx_tasks_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- VERIFY SCHEMA
-- =============================================
SHOW TABLES;

SELECT 'Database setup complete!' AS status;
