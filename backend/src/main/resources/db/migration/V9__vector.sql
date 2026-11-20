CREATE TABLE vector_store (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    content text,
    metadata json,
    embedding vector(1536)
);

CREATE INDEX vector_store_embedding_idx
    ON vector_store USING hnsw (embedding vector_cosine_ops);

CREATE TABLE embedding_task (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_id uuid NOT NULL,
    history_id bigint,
    kind varchar(10) NOT NULL CHECK (kind IN ('TICKET', 'COMMENT')),
    status varchar(10) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'DONE', 'FAILED')),
    attempts int NOT NULL DEFAULT 0,
    next_attempt_at timestamptz NOT NULL DEFAULT clock_timestamp(),
    last_error varchar(500)
);

CREATE INDEX embedding_task_status_idx ON embedding_task (status, next_attempt_at);
