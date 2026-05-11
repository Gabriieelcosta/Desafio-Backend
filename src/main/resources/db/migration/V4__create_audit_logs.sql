CREATE TABLE audit_logs (
    id            BIGSERIAL     PRIMARY KEY,
    user_id       BIGINT,
    role          VARCHAR(20),
    action        VARCHAR(100)  NOT NULL,
    entity_id     BIGINT,
    entity_type   VARCHAR(50),
    duration_ms   BIGINT,
    success       BOOLEAN       NOT NULL,
    error_message TEXT,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);
