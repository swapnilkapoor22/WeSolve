CREATE TABLE support_member (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    hash_key varchar(64) NOT NULL UNIQUE CHECK (hash_key ~ '^[0-9a-f]{64}$'),
    display_name varchar(100) NOT NULL CHECK (btrim(display_name) <> ''),
    assignable boolean NOT NULL,
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    updated_at timestamptz NOT NULL,
    updated_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL
);

CREATE TRIGGER support_member_audit
    BEFORE INSERT OR UPDATE OR DELETE ON support_member
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('full', 'id');

CREATE TABLE support_member_user (
    support_member_id uuid PRIMARY KEY REFERENCES support_member (id),
    user_hash_key varchar(64) NOT NULL UNIQUE REFERENCES users (hash_key),
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL
);

CREATE TRIGGER support_member_user_audit
    BEFORE INSERT OR DELETE ON support_member_user
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('insert-only', 'support_member_id');
