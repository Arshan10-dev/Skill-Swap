-- =============================================================
--  SkillSwap – MySQL schema (reference)
--  Hibernate creates these tables automatically on first run
--  (spring.jpa.hibernate.ddl-auto=update). This script is for
--  documentation, viva and manual setup.
-- =============================================================
CREATE DATABASE IF NOT EXISTS skillswap CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE skillswap;

CREATE TABLE users (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(100)  NOT NULL,
  email           VARCHAR(150)  NOT NULL UNIQUE,
  password        VARCHAR(255)  NOT NULL,              -- BCrypt hash
  role            VARCHAR(10)   NOT NULL DEFAULT 'USER', -- USER | ADMIN
  bio             VARCHAR(1000),
  location        VARCHAR(100),
  available_days  VARCHAR(60),                          -- e.g. MON,WED,SAT
  preferred_time  VARCHAR(20),                          -- MORNING | AFTERNOON | EVENING | FLEXIBLE
  preferred_mode  VARCHAR(20),                          -- ONLINE | IN_PERSON | BOTH
  reputation      INT           NOT NULL DEFAULT 0,
  active          BIT           NOT NULL DEFAULT 1,
  created_at      DATETIME(6)
);

CREATE TABLE categories (
  id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  name  VARCHAR(80) NOT NULL UNIQUE,
  icon  VARCHAR(30)
);

CREATE TABLE skills (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(80) NOT NULL UNIQUE,
  category_id  BIGINT      NOT NULL,
  FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Skills a user can TEACH or wants to LEARN
CREATE TABLE user_skills (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id           BIGINT      NOT NULL,
  skill_id          BIGINT      NOT NULL,
  type              VARCHAR(10) NOT NULL,   -- TEACH | LEARN
  level             VARCHAR(15) NOT NULL,   -- BEGINNER | INTERMEDIATE | EXPERT
  years_experience  INT         NOT NULL DEFAULT 0,
  UNIQUE (user_id, skill_id, type),
  FOREIGN KEY (user_id)  REFERENCES users(id),
  FOREIGN KEY (skill_id) REFERENCES skills(id)
);

CREATE TABLE swap_requests (
  id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
  sender_id            BIGINT      NOT NULL,
  receiver_id          BIGINT      NOT NULL,
  offered_skill_id     BIGINT,                 -- skill the sender teaches
  requested_skill_id   BIGINT,                 -- skill the sender wants to learn
  message              VARCHAR(500),
  status               VARCHAR(12) NOT NULL,   -- PENDING | ACCEPTED | REJECTED | CANCELLED | COMPLETED
  compatibility_score  INT         NOT NULL DEFAULT 0,
  created_at           DATETIME(6),
  responded_at         DATETIME(6),
  FOREIGN KEY (sender_id)          REFERENCES users(id),
  FOREIGN KEY (receiver_id)        REFERENCES users(id),
  FOREIGN KEY (offered_skill_id)   REFERENCES skills(id),
  FOREIGN KEY (requested_skill_id) REFERENCES skills(id)
);

CREATE TABLE messages (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  swap_request_id    BIGINT        NOT NULL,
  sender_id          BIGINT        NOT NULL,
  content            VARCHAR(2000) NOT NULL,
  read_by_recipient  BIT           NOT NULL DEFAULT 0,
  sent_at            DATETIME(6),
  FOREIGN KEY (swap_request_id) REFERENCES swap_requests(id),
  FOREIGN KEY (sender_id)       REFERENCES users(id)
);

CREATE TABLE skill_sessions (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  swap_request_id   BIGINT       NOT NULL,
  created_by_id     BIGINT       NOT NULL,
  title             VARCHAR(120) NOT NULL,
  scheduled_at      DATETIME(6)  NOT NULL,
  duration_minutes  INT          NOT NULL DEFAULT 60,
  mode              VARCHAR(12),            -- ONLINE | IN_PERSON
  location_or_link  VARCHAR(300),
  notes             VARCHAR(1000),
  status            VARCHAR(12)  NOT NULL,  -- SCHEDULED | COMPLETED | CANCELLED
  reminder_sent     BIT          NOT NULL DEFAULT 0,
  completed_at      DATETIME(6),
  FOREIGN KEY (swap_request_id) REFERENCES swap_requests(id),
  FOREIGN KEY (created_by_id)   REFERENCES users(id)
);

CREATE TABLE reviews (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id   BIGINT NOT NULL,
  reviewer_id  BIGINT NOT NULL,
  reviewee_id  BIGINT NOT NULL,
  rating       INT    NOT NULL,             -- 1..5
  comment      VARCHAR(1000),
  created_at   DATETIME(6),
  UNIQUE (session_id, reviewer_id),
  FOREIGN KEY (session_id)  REFERENCES skill_sessions(id),
  FOREIGN KEY (reviewer_id) REFERENCES users(id),
  FOREIGN KEY (reviewee_id) REFERENCES users(id)
);

CREATE TABLE badges (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT       NOT NULL,
  code         VARCHAR(40)  NOT NULL,
  name         VARCHAR(80)  NOT NULL,
  description  VARCHAR(200),
  awarded_at   DATETIME(6),
  UNIQUE (user_id, code),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE notifications (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT       NOT NULL,
  type        VARCHAR(20)  NOT NULL,   -- REQUEST | REQUEST_UPDATE | MESSAGE | SESSION | REMINDER | REVIEW | BADGE | SYSTEM
  message     VARCHAR(300) NOT NULL,
  link        VARCHAR(120),
  is_read     BIT          NOT NULL DEFAULT 0,
  created_at  DATETIME(6),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE reports (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  reporter_id  BIGINT       NOT NULL,
  reported_id  BIGINT       NOT NULL,
  reason       VARCHAR(60)  NOT NULL,
  details      VARCHAR(1000),
  status       VARCHAR(12)  NOT NULL,  -- OPEN | RESOLVED | DISMISSED
  admin_note   VARCHAR(500),
  created_at   DATETIME(6),
  resolved_at  DATETIME(6),
  FOREIGN KEY (reporter_id) REFERENCES users(id),
  FOREIGN KEY (reported_id) REFERENCES users(id)
);

CREATE TABLE blocks (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  blocker_id  BIGINT NOT NULL,
  blocked_id  BIGINT NOT NULL,
  created_at  DATETIME(6),
  UNIQUE (blocker_id, blocked_id),
  FOREIGN KEY (blocker_id) REFERENCES users(id),
  FOREIGN KEY (blocked_id) REFERENCES users(id)
);
