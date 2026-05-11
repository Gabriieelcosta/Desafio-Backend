CREATE TABLE analyst_coverage (
    id          BIGSERIAL PRIMARY KEY,
    analyst_id  BIGINT      NOT NULL REFERENCES users(id),
    state       CHAR(2)     NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_analyst_state UNIQUE (analyst_id, state)
);
