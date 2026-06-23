# TaskTracker — Full-Stack Task Management Application

A production-ready task tracking web application built with **Java Spring Boot** (backend) and **Vanilla HTML/CSS/JavaScript** (frontend). Designed as a portfolio project demonstrating clean architecture, Spring Security, JPA relationships, and modern UI design.

---

## Tech Stack

| Layer      | Technology                              |
|------------|-----------------------------------------|
| Backend    | Java 17, Spring Boot 3.2, Spring MVC   |
| Security   | Spring Security 6, BCrypt               |
| ORM        | Spring Data JPA, Hibernate              |
| Database   | MySQL 8+                                |
| Templates  | Thymeleaf                               |
| Frontend   | Vanilla HTML5, CSS3, JavaScript (ES6+) |

---

## Features

- ✅ User registration, login, logout with session management
- ✅ Forgot password / reset password flow (token-based)
- ✅ BCrypt password hashing (strength factor 12)
- ✅ Full task CRUD (Create, Read, Update, Delete)
- ✅ Task priorities: Low, Medium, High, Critical
- ✅ Task statuses: Pending, In Progress, Completed, Overdue (auto-detected)
- ✅ Custom categories per user
- ✅ Dashboard with 6 live statistics + completion ring
- ✅ Filter tasks by search, status, priority, category
- ✅ Due-soon warnings (24h, 3-day highlights)
- ✅ User data isolation (users never see each other's data)
- ✅ Responsive mobile-friendly layout
- ✅ CSRF protection on all forms

---

## Project Structure

```
task-tracker/
├── pom.xml
├── Dockerfile                              # Multi-stage build for deployment
├── render.yaml                             # Render Blueprint (one-click deploy config)
├── .gitignore
├── .dockerignore
├── README.md
├── DEPLOYMENT.md                           # Free hosting walkthrough (Aiven + Render)
├── docs/
│   ├── schema.sql                          # Local MySQL setup script
│   └── schema-cloud.sql                    # Cloud-provider-safe setup script
└── src/main/
    ├── java/com/tasktracker/
    │   ├── TaskTrackerApplication.java
    │   ├── controller/
    │   │   ├── AuthController.java
    │   │   ├── DashboardController.java
    │   │   ├── TaskController.java
    │   │   ├── CategoryController.java
    │   │   ├── HomeController.java
    │   │   └── CustomErrorController.java  # Replaces Whitelabel error page
    │   ├── service/
    │   │   ├── UserService.java
    │   │   ├── TaskService.java
    │   │   ├── CategoryService.java
    │   │   └── EmailService.java           # Password reset emails
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   ├── TaskRepository.java
    │   │   └── CategoryRepository.java
    │   ├── entity/
    │   │   ├── User.java
    │   │   ├── Task.java          (Priority + Status enums)
    │   │   └── Category.java
    │   ├── dto/
    │   │   ├── RegisterRequest.java
    │   │   ├── TaskRequest.java
    │   │   ├── DashboardStats.java
    │   │   └── PasswordDtos.java
    │   ├── exception/
    │   │   └── GlobalExceptionHandler.java
    │   └── security/
    │       ├── SecurityConfig.java
    │       ├── CustomUserDetailsService.java
    │       ├── SecurityUtils.java
    │       ├── RateLimiter.java             # In-memory abuse throttling
    │       └── LoginThrottleFailureHandler.java
    └── resources/
        ├── application.properties          # Env-var driven, no secrets
        ├── application-prod.properties     # Production profile overrides
        ├── application-local.properties.template
        ├── templates/
        │   ├── fragments/
        │   │   └── layout.html            # Sidebar + head fragments
        │   ├── auth/
        │   │   ├── login.html
        │   │   ├── register.html
        │   │   ├── forgot-password.html
        │   │   └── reset-password.html
        │   ├── tasks/
        │   │   ├── list.html
        │   │   ├── create.html
        │   │   └── edit.html
        │   ├── error/
        │   │   └── generic.html
        │   └── dashboard.html
        └── static/
            ├── css/main.css
            └── js/main.js
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+

---

## Setup Instructions

### Step 1: Clone / Download the Project

```bash
# If downloaded as zip, extract to a folder, e.g.:
cd ~/projects/task-tracker
```

### Step 2: Set Up the Database

For **local development**, open MySQL and run:

```bash
mysql -u root -p < docs/schema.sql
```

Or paste the contents of `docs/schema.sql` into MySQL Workbench / DBeaver.

This creates:
- Database: `tasktracker`
- Tables: `users`, `categories`, `tasks`

> For a **cloud/managed database** (Aiven, RDS, etc.) where you can't `CREATE DATABASE`, use `docs/schema-cloud.sql` instead — see `DEPLOYMENT.md`.

### Step 3: Set Your Database Credentials (Environment Variables)

**Never edit `application.properties` directly with real credentials** — it's tracked in git and that's exactly how secrets leak. Instead, set environment variables.

**macOS / Linux:**
```bash
export DB_URL="jdbc:mysql://localhost:3306/tasktracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_USERNAME="root"
export DB_PASSWORD="your_actual_password"
```

**Windows (PowerShell):**
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/tasktracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_actual_password"
```

Or copy `src/main/resources/application-local.properties.template` to `application-local.properties` (already gitignored) and fill in your password there, then run with `-Dspring-boot.run.profiles=local`.

### Step 4: Build and Run

```bash
# From the project root (where pom.xml is)
mvn clean install -DskipTests
mvn spring-boot:run
```

Or in IntelliJ IDEA / Eclipse:
- Open the project
- Set the same environment variables in your Run Configuration
- Run `TaskTrackerApplication.java` as a Spring Boot app

### Step 5: Open in Browser

```
http://localhost:8080
```

You'll be redirected to the login page. Click **"Create one"** to register.

---

## Deploying to Production

See **[DEPLOYMENT.md](./DEPLOYMENT.md)** for a complete, free walkthrough using:
- **Aiven** (free managed MySQL)
- **Render** (free Docker hosting)

---

## Password Reset

Password resets are sent via real email using Spring Mail, **once you configure SMTP credentials** (see `DEPLOYMENT.md` Part 5 for free Gmail setup).

Until then — or in local dev with no email configured — reset links are written to the **application log** instead, so the flow stays fully testable:

```
INFO  c.t.service.EmailService - [DEV MODE - email disabled] Password reset link for user@example.com: http://localhost:8080/auth/reset-password?token=abc123...
```

Copy that URL into your browser to reset the password.

---

## Default Categories

When a user registers, these categories are automatically created for them:
- Work
- Personal
- School
- Fitness
- Finance

Users can add more or delete any from the task list page.

---

## Security Notes

- Passwords are hashed with **BCrypt (cost factor 12)**
- Sessions expire after **30 minutes** of inactivity; cookies are `HttpOnly` and `Secure` in production
- All routes except auth pages require authentication
- CSRF tokens are included on all forms
- Users can only access their own tasks and categories
- Password reset tokens expire after **24 hours**
- Basic in-memory rate limiting on login, registration, and forgot-password
- Security headers (HSTS, frame options) applied in production
- Global exception handler — users never see raw stack traces or the Spring Whitelabel error page

---

## Creating a Dedicated MySQL User (Recommended)

Instead of using root:

```sql
CREATE USER 'tasktracker'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT ALL PRIVILEGES ON tasktracker.* TO 'tasktracker'@'localhost';
FLUSH PRIVILEGES;
```

Then set the environment variables (see Step 3 above) instead of editing any file:
```bash
export DB_USERNAME="tasktracker"
export DB_PASSWORD="StrongPassword123!"
```

---

## Production Checklist

Already handled in this codebase:
- ✅ `spring.jpa.hibernate.ddl-auto=validate` in the `prod` profile (not `update`)
- ✅ All secrets read from environment variables — nothing hardcoded
- ✅ Real email service for password resets (Spring Mail, see `DEPLOYMENT.md`)
- ✅ `spring.thymeleaf.cache=true` in the `prod` profile
- ✅ Multi-stage `Dockerfile` ready to deploy

For the full free-hosting walkthrough (Aiven MySQL + Render), see **[DEPLOYMENT.md](./DEPLOYMENT.md)**.
