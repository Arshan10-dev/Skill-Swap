# SkillSwap – A Skill Exchange and Matching Platform

Full-stack web application: **React.js + Bootstrap 5** frontend, **Java Spring Boot** REST backend, **MySQL** database.

Users list skills they can teach and skills they want to learn. A matching engine scores compatible partners. Then they send swap requests, chat, schedule sessions, rate each other and build reputation. Admins manage users, skills, reports and analytics.

```
skillswap/
├── backend/        Spring Boot 3 (Java 17+), Maven
├── frontend/       React 18 + Vite + Bootstrap 5 + React Router + Axios
├── database/       schema.sql (reference MySQL schema)
└── launch-video/   SkillSwap_Launch.mp4 + the code that renders it
```

---

## 1. Prerequisites

| Tool | Version | Check with | Expected output |
|---|---|---|---|
| Java JDK | 17 or newer | `java -version` | `openjdk version "17..."` or higher |
| Maven | 3.8+ | `mvn -v` | `Apache Maven 3.x` |
| Node.js | 18+ | `node -v` | `v18...` or higher |
| MySQL | 8.x | `mysql --version` | `mysql  Ver 8...` |

> **macOS install (Homebrew):** `brew install openjdk@17 maven node mysql` then `brew services start mysql`

---

## 2. Database (MySQL)

The tables are created automatically by Hibernate on first start, so you only need a running MySQL server and a login.

1. Log in: `mysql -u root -p`
2. Create the database (optional, the app also creates it automatically):
   ```sql
   CREATE DATABASE skillswap;
   ```
   Expected: `Query OK, 1 row affected`
3. If your MySQL user or password is not `root` / `root`, either edit
   `backend/src/main/resources/application.properties` or set environment variables:
   ```bash
   export DB_USER=root
   export DB_PASSWORD=yourpassword
   ```

`database/schema.sql` documents every table (for the report and viva). You don't need to run it.

---

## 3. Run the backend

> ⚠️ Check the prompt before pasting: you should be inside the `skillswap/backend` folder.

```bash
cd backend
mvn spring-boot:run
```

First run downloads dependencies (a few minutes). **Expected output at the end:**
```
Seed complete. Admin: admin@skillswap.com / admin123, demo users: <name>@demo.com / password123
Tomcat started on port 8080 (http)
Started SkillSwapApplication in ... seconds
```
Quick test (in a new terminal): `curl http://localhost:8080/api/stats/public`
Expected: `{"users":9,"skills":39,"completedSessions":3}`

**No MySQL yet?** Run with the in-memory H2 database instead (data resets on restart):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

---

## 4. Run the frontend

> ⚠️ Open a **second** terminal. Check the prompt: you should be inside `skillswap/frontend`.

```bash
cd frontend
npm install
npm run dev
```
Expected:
```
VITE v5.x  ready in ... ms
➜  Local:   http://localhost:5173/
```
Open **http://localhost:5173**. The dev server proxies `/api` to the backend on port 8080.

### Demo accounts (seeded automatically)
| Role | Email | Password |
|---|---|---|
| Admin | admin@skillswap.com | admin123 |
| User | arshan@demo.com | password123 |
| User | rashid@demo.com | password123 |
| More users | priya@, aman@, sara@, kabir@, zoya@, rohan@ `demo.com` | password123 |

The login page has one-click buttons that fill these in.

---

## 5. Feature checklist (proposal → implementation)

| # | Feature | Where |
|---|---|---|
| 1 | Registration & login (JWT, BCrypt) | `AuthController`, `security/*`, `Login.jsx`, `Register.jsx` |
| 2 | Profile management | `UserController`, `Profile.jsx` |
| 3 | Skill management (teach / learn, level, years) | `SkillController`, `MySkills.jsx` |
| 4 | Skill categories | `Category` entity, admin page |
| 5 | Search & filtering | `SearchController`, `Search.jsx` |
| 6 | Intelligent matching | `MatchingService` |
| 7 | Compatibility score (0–100) | `MatchingService.score()` |
| 8 | Recommended partners | `MatchController`, `Matches.jsx`, dashboard |
| 9 | Swap requests: send / accept / reject / cancel / complete | `SwapService`, `Requests.jsx` |
| 10 | Chat between matched users | `RequestController` messages, `Chat.jsx` |
| 11 | Session scheduling (online / in person) | `SessionService`, `Sessions.jsx` |
| 12 | Session history | Sessions → History tab |
| 13 | Ratings & reviews | `ReviewService`, review modal |
| 14 | Reputation system | `ReputationService` |
| 15 | Badges & achievements (8 badges) | `ReputationService.evaluateBadges()` |
| 16 | Notifications + 1-hour session reminders | `NotificationService`, `SessionService.sendReminders()` |
| 17 | Report & block | `SafetyController`, user profile page, Settings |
| 18 | Admin dashboard: users, skills, reports | `AdminController`, `pages/admin/*` |
| 19 | Analytics | `/api/admin/analytics`, `AdminDashboard.jsx` |
| ★ | Optional: WebSocket push | `WebSocketConfig` (STOMP at `/ws`, topic `/topic/notifications/{userId}`) |

### How the compatibility score works
For you (A) and a candidate (B):

| Points | Factor |
|---|---|
| 40 | Share of A's "want to learn" skills that B teaches |
| 25 | Share of B's "want to learn" skills that A teaches |
| 10 | Mutual bonus when both directions have a skill |
| 10 | B's teaching level on those skills (Expert 1.0, Intermediate 0.7, Beginner 0.4) |
| 10 | Overlap of available days |
| 5 | Same preferred time and mode |
| +5 | Reputation nudge (max), capped at 100 |

Blocked users, suspended users and admins are never recommended.

### Reputation
+20 per completed session (both users) · +4 × stars for each review received · +5 for writing a review.

---

## 6. REST API summary

All endpoints are under `/api`. Send `Authorization: Bearer <token>` except for auth and public GETs.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/auth/register`, `/auth/login` | Get a JWT |
| GET/PUT | `/users/me` | My profile |
| PUT | `/users/me/password` | Change password |
| GET | `/users/{id}`, `/users/{id}/reviews` | Public profile, reviews |
| GET | `/categories`, `/skills` | Catalogue |
| GET/POST/PUT/DELETE | `/me/skills[/{id}]` | My teach/learn skills |
| GET | `/matches?skillId=&minScore=&limit=` | Recommendations |
| GET | `/search?q=&categoryId=&level=&location=&day=&type=` | Member search |
| POST | `/requests` | Send swap request |
| GET | `/requests/incoming`, `/outgoing`, `/active` | Lists |
| POST | `/requests/{id}/accept`, `reject`, `cancel`, `complete` | Workflow |
| GET/POST | `/requests/{id}/messages` | Chat |
| GET/POST/PUT | `/sessions[/{id}]` | Sessions |
| POST | `/sessions/{id}/complete`, `/cancel` | Session status |
| POST | `/reviews` | Rate a partner |
| GET/POST | `/notifications`, `/unread-count`, `/{id}/read`, `/read-all` | Notifications |
| POST | `/reports` · GET/POST/DELETE `/blocks[/{userId}]` | Safety |
| * | `/admin/users`, `/admin/skills`, `/admin/categories`, `/admin/reports`, `/admin/analytics` | Admin only |

---

## 7. Launch video

`launch-video/SkillSwap_Launch.mp4`: a 32-second, 1080p promo with sound, built entirely in code (HTML + GSAP, rendered frame-by-frame with Playwright, audio synthesized in Python).
To edit and re-render: change `launch-video/index.html`, serve the folder (`python3 -m http.server 4200`), run `node render.js all frames`, then `python3 sfx.py` and
`ffmpeg -framerate 30 -i frames/%04d.jpg -i sfx.wav -c:v libx264 -crf 18 -pix_fmt yuv420p -c:a aac -shortest out.mp4`.
