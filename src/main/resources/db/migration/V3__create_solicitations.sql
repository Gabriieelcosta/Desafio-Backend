CREATE TABLE solicitations (
    id               BIGSERIAL     PRIMARY KEY,
    client_id        BIGINT        NOT NULL REFERENCES users(id),
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    current_step     SMALLINT      NOT NULL DEFAULT 0,

    -- Step 1
    service_type     VARCHAR(20),
    title            VARCHAR(80),
    description      TEXT,

    -- Step 2
    cep              VARCHAR(9),
    number           VARCHAR(20),
    complement       VARCHAR(100),
    street           VARCHAR(200),
    neighborhood     VARCHAR(100),
    city             VARCHAR(100),
    state            CHAR(2),

    -- Step 3
    priority         VARCHAR(10),
    preferred_date   DATE,
    estimated_value  NUMERIC(12, 2),
    terms_accepted   BOOLEAN,

    -- Auditoria
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP,
    submitted_at     TIMESTAMP,
    analyzed_at      TIMESTAMP,
    analyzed_by      BIGINT        REFERENCES users(id),
    analysis_comment TEXT
);
