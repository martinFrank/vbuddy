CREATE TABLE vbuddy_task (
    id                BIGSERIAL PRIMARY KEY,
    buddy_id          BIGINT NOT NULL REFERENCES buddy(id) ON DELETE CASCADE,
    title             VARCHAR(200) NOT NULL,
    description       TEXT NOT NULL,
    location          VARCHAR(200) NOT NULL,
    start_time        TIMESTAMP NOT NULL,
    duration_minutes  INTEGER NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_vbuddy_task_buddy ON vbuddy_task(buddy_id);
CREATE INDEX idx_vbuddy_task_buddy_status ON vbuddy_task(buddy_id, status);
CREATE INDEX idx_vbuddy_task_start ON vbuddy_task(buddy_id, start_time);
