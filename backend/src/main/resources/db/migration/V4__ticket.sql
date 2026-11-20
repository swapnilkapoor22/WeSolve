CREATE SEQUENCE ticket_number_seq;

CREATE TABLE ticket (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_number bigint NOT NULL UNIQUE DEFAULT nextval('ticket_number_seq'),
    title varchar(150) NOT NULL CHECK (btrim(title) <> ''),
    description varchar(5000) NOT NULL CHECK (btrim(description) <> ''),
    priority varchar(10) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status varchar(12) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED')),
    assignee_id uuid NOT NULL REFERENCES support_member (id),
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    updated_at timestamptz NOT NULL,
    updated_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL
);

CREATE INDEX ticket_assignee_status_idx ON ticket (assignee_id, status, ticket_number DESC);
CREATE INDEX ticket_status_number_idx ON ticket (status, ticket_number DESC);
CREATE INDEX ticket_number_desc_idx ON ticket (ticket_number DESC);

CREATE FUNCTION ticket_status_guard() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.status IS NOT DISTINCT FROM OLD.status THEN
        RETURN NEW;
    END IF;
    IF (OLD.status = 'OPEN' AND NEW.status IN ('IN_PROGRESS', 'CANCELLED'))
        OR (OLD.status = 'IN_PROGRESS' AND NEW.status IN ('RESOLVED', 'CANCELLED'))
        OR (OLD.status = 'RESOLVED' AND NEW.status = 'CLOSED') THEN
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'status change not allowed from % to %', OLD.status, NEW.status;
END;
$$;

CREATE TRIGGER ticket_status_guard
    BEFORE UPDATE OF status ON ticket
    FOR EACH ROW EXECUTE FUNCTION ticket_status_guard();

CREATE TRIGGER ticket_audit
    BEFORE INSERT OR UPDATE OR DELETE ON ticket
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('full', 'id');
