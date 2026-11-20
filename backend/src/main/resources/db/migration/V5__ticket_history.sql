CREATE TABLE ticket_history (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_id uuid NOT NULL REFERENCES ticket (id),
    actor_id uuid NOT NULL REFERENCES support_member (id),
    from_status varchar(12),
    to_status varchar(12),
    from_assignee_id uuid,
    to_assignee_id uuid,
    comment varchar(2000),
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL,
    CONSTRAINT ticket_history_something CHECK (
        to_status IS NOT NULL OR to_assignee_id IS NOT NULL OR comment IS NOT NULL
    ),
    CONSTRAINT ticket_history_comment_text CHECK (
        comment IS NULL OR btrim(comment) <> ''
    ),
    CONSTRAINT ticket_history_resolve_comment CHECK (
        to_status IS NULL
        OR to_status NOT IN ('RESOLVED', 'CANCELLED')
        OR (comment IS NOT NULL AND btrim(comment) <> '')
    ),
    CONSTRAINT ticket_history_status_pair CHECK (
        ((from_status IS NULL) = (to_status IS NULL))
        OR (
            from_status IS NULL
            AND to_status IS NOT NULL
            AND from_assignee_id IS NULL
            AND to_assignee_id IS NOT NULL
        )
    ),
    CONSTRAINT ticket_history_assignee_pair CHECK (
        ((from_assignee_id IS NULL) = (to_assignee_id IS NULL))
        OR (
            from_assignee_id IS NULL
            AND to_assignee_id IS NOT NULL
            AND from_status IS NULL
            AND to_status IS NOT NULL
        )
    )
);

CREATE INDEX ticket_history_ticket_idx ON ticket_history (ticket_id, id);

CREATE TRIGGER ticket_history_audit
    BEFORE INSERT ON ticket_history
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('insert-only', 'id');

CREATE TRIGGER ticket_history_no_update
    BEFORE UPDATE ON ticket_history
    FOR EACH ROW EXECUTE FUNCTION reject_mutation();

CREATE TRIGGER ticket_history_no_delete
    BEFORE DELETE ON ticket_history
    FOR EACH ROW EXECUTE FUNCTION reject_mutation();
