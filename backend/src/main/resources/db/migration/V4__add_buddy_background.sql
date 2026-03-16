CREATE TABLE buddy_background (
    id          BIGSERIAL PRIMARY KEY,
    buddy_id    BIGINT NOT NULL UNIQUE REFERENCES buddy(id) ON DELETE CASCADE,
    structured_data TEXT,
    narrative_text  TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_buddy_background_buddy_id ON buddy_background(buddy_id);
