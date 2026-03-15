CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE buddy (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    personality TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE need (
    id                 BIGSERIAL PRIMARY KEY,
    buddy_id           BIGINT NOT NULL REFERENCES buddy(id) ON DELETE CASCADE,
    need_type          VARCHAR(50) NOT NULL,
    current_value      DOUBLE PRECISION NOT NULL DEFAULT 50.0,
    max_value          DOUBLE PRECISION NOT NULL DEFAULT 100.0,
    decay_rate_per_hour DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    updated_at         TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (buddy_id, need_type)
);

CREATE TABLE daily_plan (
    id         BIGSERIAL PRIMARY KEY,
    buddy_id   BIGINT NOT NULL REFERENCES buddy(id) ON DELETE CASCADE,
    plan_date  DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (buddy_id, plan_date)
);

CREATE TABLE activity (
    id            BIGSERIAL PRIMARY KEY,
    daily_plan_id BIGINT NOT NULL REFERENCES daily_plan(id) ON DELETE CASCADE,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    start_time    TIME NOT NULL,
    end_time      TIME NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE blog_post (
    id          BIGSERIAL PRIMARY KEY,
    buddy_id    BIGINT NOT NULL REFERENCES buddy(id) ON DELETE CASCADE,
    activity_id BIGINT REFERENCES activity(id) ON DELETE SET NULL,
    title       VARCHAR(300) NOT NULL,
    content     TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE chat_message (
    id         BIGSERIAL PRIMARY KEY,
    buddy_id   BIGINT NOT NULL REFERENCES buddy(id) ON DELETE CASCADE,
    role       VARCHAR(20) NOT NULL,
    content    TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ai_decision_log (
    id         BIGSERIAL PRIMARY KEY,
    buddy_id   BIGINT NOT NULL REFERENCES buddy(id) ON DELETE CASCADE,
    context    TEXT NOT NULL,
    decision   TEXT NOT NULL,
    reasoning  TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_need_buddy ON need(buddy_id);
CREATE INDEX idx_daily_plan_buddy_date ON daily_plan(buddy_id, plan_date);
CREATE INDEX idx_activity_plan ON activity(daily_plan_id);
CREATE INDEX idx_blog_post_buddy ON blog_post(buddy_id);
CREATE INDEX idx_chat_message_buddy ON chat_message(buddy_id);
CREATE INDEX idx_ai_decision_log_buddy ON ai_decision_log(buddy_id);
