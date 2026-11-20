CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE audit_watermark (
    id int PRIMARY KEY,
    value bigint NOT NULL
);

INSERT INTO audit_watermark (id, value) VALUES (1, 0);

CREATE TABLE audit_change (
    watermark bigint PRIMARY KEY,
    changed_at timestamptz NOT NULL,
    actor_hash varchar(64) NOT NULL,
    table_name varchar(63) NOT NULL,
    operation varchar(6) NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    row_key text NOT NULL,
    before_data jsonb,
    after_data jsonb,
    transaction_id bigint NOT NULL
);

CREATE INDEX audit_change_row_idx ON audit_change (table_name, row_key, watermark);

CREATE FUNCTION reject_mutation() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION '% is immutable', TG_TABLE_NAME;
END;
$$;

CREATE TRIGGER audit_change_no_update
    BEFORE UPDATE ON audit_change
    FOR EACH ROW EXECUTE FUNCTION reject_mutation();

CREATE TRIGGER audit_change_no_delete
    BEFORE DELETE ON audit_change
    FOR EACH ROW EXECUTE FUNCTION reject_mutation();

CREATE FUNCTION wesolve_audit() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    actor text;
    wm bigint;
    mode text;
    keys text[];
    key_name text;
    key_text text;
    part text;
    payload jsonb;
    source jsonb;
BEGIN
    mode := TG_ARGV[0];
    actor := nullif(current_setting('app.actor', true), '');
    IF actor IS NULL THEN
        actor := 'system';
    END IF;

    UPDATE audit_watermark SET value = value + 1 WHERE id = 1 RETURNING value INTO wm;

    IF TG_OP = 'INSERT' THEN
        NEW.created_at := clock_timestamp();
        NEW.created_by := actor;
        IF mode = 'full' THEN
            NEW.updated_at := NEW.created_at;
            NEW.updated_by := actor;
        END IF;
        NEW.last_watermark := wm;
        payload := to_jsonb(NEW);
    ELSIF TG_OP = 'UPDATE' THEN
        IF mode = 'full' THEN
            NEW.created_at := OLD.created_at;
            NEW.created_by := OLD.created_by;
            NEW.updated_at := clock_timestamp();
            NEW.updated_by := actor;
        END IF;
        NEW.last_watermark := wm;
        payload := to_jsonb(NEW);
    ELSE
        payload := NULL;
    END IF;

    IF TG_OP = 'DELETE' THEN
        source := to_jsonb(OLD);
    ELSE
        source := payload;
    END IF;

    keys := string_to_array(TG_ARGV[1], '|');
    key_text := '';
    FOREACH key_name IN ARRAY keys LOOP
        part := source ->> key_name;
        IF key_text <> '' THEN
            key_text := key_text || '|';
        END IF;
        key_text := key_text || coalesce(part, '');
    END LOOP;

    INSERT INTO audit_change (
        watermark, changed_at, actor_hash, table_name, operation, row_key,
        before_data, after_data, transaction_id
    ) VALUES (
        wm,
        clock_timestamp(),
        actor,
        TG_TABLE_NAME,
        TG_OP,
        key_text,
        CASE WHEN TG_OP = 'INSERT' THEN NULL ELSE to_jsonb(OLD) END,
        CASE WHEN TG_OP = 'DELETE' THEN NULL ELSE payload END,
        txid_current()
    );

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$;
