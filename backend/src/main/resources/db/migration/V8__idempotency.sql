CREATE TABLE idempotency_record (
    actor_hash varchar(64) NOT NULL,
    idempotency_key varchar(100) NOT NULL,
    request_hash varchar(64) NOT NULL,
    response_status int NOT NULL,
    response_body jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (actor_hash, idempotency_key)
);
