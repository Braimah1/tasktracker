# Deploying TaskTracker to Production (Free Tier)

This guide walks through hosting TaskTracker for free using:
- **Aiven** for a free managed MySQL database
- **Render** for free Docker-based app hosting

Total cost: **$0**. Both have generous always-free tiers as of 2026.

> **Important:** I (the AI) cannot create accounts or click through web consoles on your behalf — I don't have the ability to sign up for services or hold your credentials. Everything below is written so you can do it yourself in about 20–30 minutes by following exact steps.

---

## Part 1 — Create a free MySQL database on Aiven

1. Go to **https://aiven.io** and sign up (free, no credit card required for the free plan).
2. Click **Create service** → choose **MySQL**.
3. Plan: select **Free** (1 CPU, 1 GB storage — plenty for this app).
4. Choose any cloud/region close to you, name the service e.g. `tasktracker-db`.
5. Click **Create service**. It takes 2–5 minutes to provision.
6. Once it's running, open the service page → **Overview** tab. You'll see connection details:
   - **Host**
   - **Port**
   - **User** (usually `avnadmin`)
   - **Password**
   - **Default database name** (usually `defaultdb`)

7. Build your JDBC URL from these values:
   ```
   jdbc:mysql://<HOST>:<PORT>/defaultdb?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
   ```
   Aiven requires SSL — note `useSSL=true&requireSSL=true` (different from the local dev URL).

8. **Create the schema.** Aiven gives you a web-based SQL console, or you can connect locally:
   ```bash
   mysql --host=<HOST> --port=<PORT> --user=<USER> -p --ssl-mode=REQUIRED defaultdb < docs/schema.sql
   ```
   Replace `USE tasktracker;` in `docs/schema.sql` with `USE defaultdb;` first (or just remove the `CREATE DATABASE`/`USE` lines since Aiven already gives you `defaultdb`).

Keep this tab open — you'll paste these values into Render in Part 3.

---

## Part 2 — Push your code to GitHub

Render deploys from a Git repo, so the code needs to be on GitHub (or GitLab/Bitbucket).

```bash
cd tasktracker
git init
git add .
git commit -m "Production-ready TaskTracker"
```

Create a new empty repo on https://github.com/new (don't initialize with a README), then:

```bash
git remote add origin https://github.com/YOUR_USERNAME/tasktracker.git
git branch -M main
git push -u origin main
```

**Before you push**, double check no secrets are committed:
```bash
git log -p | grep -i "password\|Simitimi" 
```
If that returns nothing, you're clean. The `.gitignore` included now prevents this going forward.

---

## Part 3 — Deploy to Render

### Option A: One-click via Blueprint (recommended)

1. Go to **https://render.com** and sign up / log in.
2. Click **New** → **Blueprint**.
3. Connect your GitHub account and select the `tasktracker` repo.
4. Render will detect `render.yaml` automatically and show the services it will create.
5. You'll be prompted to fill in the environment variables marked `sync: false`:

   | Variable | Value |
   |---|---|
   | `DB_URL` | `jdbc:mysql://<AIVEN_HOST>:<PORT>/defaultdb?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
   | `DB_USERNAME` | `avnadmin` (or your Aiven user) |
   | `DB_PASSWORD` | your Aiven password |
   | `APP_BASE_URL` | leave blank for now — you'll fill this in after first deploy once Render gives you a URL |
   | `MAIL_USERNAME` | leave blank for now (see Part 4) |
   | `MAIL_PASSWORD` | leave blank for now |
   | `MAIL_FROM` | leave blank for now |

6. Click **Apply**. Render will build the Docker image and deploy. First build takes 3–6 minutes.

### Option B: Manual web service (if Blueprint isn't available on your account)

1. **New** → **Web Service** → connect the repo.
2. Runtime: **Docker** (Render auto-detects the `Dockerfile`).
3. Plan: **Free**.
4. Add the same environment variables as the table above, plus:
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `DDL_AUTO` = `validate`
   - `MAIL_ENABLED` = `false`
5. Health check path: `/actuator/health`
6. Click **Create Web Service**.

---

## Part 4 — After first deploy

1. Render will give you a public URL like `https://tasktracker-xxxx.onrender.com`.
2. Go back into the service's **Environment** settings and set:
   - `APP_BASE_URL` = `https://tasktracker-xxxx.onrender.com`
   
   This is used to build correct password-reset links in emails.
3. Render will auto-redeploy when you save environment variable changes.

### Free-tier sleep behavior

Render's free web services **spin down after 15 minutes of inactivity** and take ~30–60 seconds to wake back up on the next request. This is normal for the free tier — there's no workaround without upgrading to a paid plan. For a portfolio/demo project this is usually fine; just mention it if you're sharing the link with someone (the first load might be slow).

---

## Part 5 — (Optional) Enable real password-reset emails

Without this, password reset links are written to the Render application logs instead of emailed — functional for testing, but not for real users.

The easiest free option is a **Gmail App Password**:

1. Enable 2-Step Verification on the Gmail account you want to send from: https://myaccount.google.com/security
2. Go to https://myaccount.google.com/apppasswords and create an app password for "Mail".
3. In Render's environment variables, set:
   - `MAIL_ENABLED` = `true`
   - `MAIL_USERNAME` = your full Gmail address
   - `MAIL_PASSWORD` = the 16-character app password (not your normal Gmail password)
   - `MAIL_FROM` = same Gmail address

Gmail's free sending limit is ~500 emails/day, far more than a portfolio app needs.

---

## Part 6 — Verify everything works

1. Visit your Render URL.
2. Register a new account.
3. Confirm you land on the dashboard.
4. Create a task, mark it complete, delete it.
5. Log out, then try "Forgot password" — check Render's **Logs** tab for the reset link if you haven't set up email yet.
6. Hit `https://your-app.onrender.com/actuator/health` — should return `{"status":"UP"}`.

---

## Updating the app later

Any `git push` to `main` triggers an automatic redeploy on Render. No extra steps needed.

---

## Summary of what changed for production-readiness

- Removed the hardcoded MySQL password from `application.properties`; all secrets now come from environment variables
- Added `.gitignore` so secrets/build output never get committed again
- Added a `prod` Spring profile (`application-prod.properties`) — secure cookies, schema validation instead of auto-alter, reduced logging
- Added real email sending for password resets (Spring Mail), with safe fallback to logging when not configured
- Added a global exception handler + custom error page so users never see raw stack traces or the Spring Whitelabel page
- Added basic in-memory rate limiting on login, registration, and forgot-password to blunt brute-force/spam
- Added security headers (HSTS, frame options) and proxy-aware HTTPS handling for Render's TLS-terminating edge
- Added Spring Boot Actuator `/actuator/health` for Render's health checks
- Added a multi-stage `Dockerfile` (non-root user, small JRE-alpine runtime image) and `render.yaml` Blueprint for repeatable deploys
