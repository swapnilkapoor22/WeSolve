CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    hash_key varchar(64) NOT NULL UNIQUE CHECK (hash_key ~ '^[0-9a-f]{64}$'),
    sign_in_id varchar(100) NOT NULL UNIQUE,
    display_name varchar(100) NOT NULL CHECK (btrim(display_name) <> ''),
    email varchar(254) NOT NULL UNIQUE CHECK (email ~ '.+@.+'),
    active boolean NOT NULL DEFAULT true,
    can_be_assigned boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    updated_at timestamptz NOT NULL,
    updated_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL
);

CREATE INDEX users_display_name_lower_idx ON users (lower(display_name));

CREATE FUNCTION users_normalize() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    NEW.sign_in_id := lower(btrim(NEW.sign_in_id));
    NEW.email := lower(btrim(NEW.email));
    IF TG_OP = 'UPDATE' THEN
        IF NEW.sign_in_id IS DISTINCT FROM OLD.sign_in_id THEN
            RAISE EXCEPTION 'sign_in_id is immutable';
        END IF;
        IF NEW.hash_key IS DISTINCT FROM OLD.hash_key THEN
            RAISE EXCEPTION 'hash_key is immutable';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER users_00_normalize
    BEFORE INSERT OR UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION users_normalize();

CREATE TRIGGER users_10_audit
    BEFORE INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('full', 'id');

CREATE TABLE groups (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(100) NOT NULL CHECK (btrim(name) <> ''),
    active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    updated_at timestamptz NOT NULL,
    updated_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL
);

CREATE UNIQUE INDEX groups_name_lower_idx ON groups (lower(name));

CREATE TRIGGER groups_audit
    BEFORE INSERT OR UPDATE OR DELETE ON groups
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('full', 'id');

CREATE TABLE roles (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(100) NOT NULL CHECK (btrim(name) <> ''),
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    updated_at timestamptz NOT NULL,
    updated_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL
);

CREATE UNIQUE INDEX roles_name_lower_idx ON roles (lower(name));

CREATE TRIGGER roles_audit
    BEFORE INSERT OR UPDATE OR DELETE ON roles
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('full', 'id');

CREATE TABLE access_types (
    code varchar(40) PRIMARY KEY,
    description varchar(200) NOT NULL,
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    updated_at timestamptz NOT NULL,
    updated_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL
);

CREATE TRIGGER access_types_audit
    BEFORE INSERT OR UPDATE OR DELETE ON access_types
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('full', 'code');

CREATE TABLE user_group (
    user_hash_key varchar(64) NOT NULL REFERENCES users (hash_key),
    group_id uuid NOT NULL REFERENCES groups (id),
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL,
    PRIMARY KEY (user_hash_key, group_id)
);

CREATE INDEX user_group_group_idx ON user_group (group_id);

CREATE TRIGGER user_group_audit
    BEFORE INSERT OR DELETE ON user_group
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('insert-only', 'user_hash_key|group_id');

CREATE TABLE group_role (
    group_id uuid NOT NULL REFERENCES groups (id),
    role_id uuid NOT NULL REFERENCES roles (id),
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL,
    PRIMARY KEY (group_id, role_id)
);

CREATE INDEX group_role_role_idx ON group_role (role_id);

CREATE TRIGGER group_role_audit
    BEFORE INSERT OR DELETE ON group_role
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('insert-only', 'group_id|role_id');

CREATE TABLE role_access (
    role_id uuid NOT NULL REFERENCES roles (id),
    access_code varchar(40) NOT NULL REFERENCES access_types (code),
    created_at timestamptz NOT NULL,
    created_by varchar(64) NOT NULL,
    last_watermark bigint NOT NULL,
    PRIMARY KEY (role_id, access_code)
);

CREATE INDEX role_access_code_idx ON role_access (access_code);

CREATE TRIGGER role_access_audit
    BEFORE INSERT OR DELETE ON role_access
    FOR EACH ROW EXECUTE FUNCTION wesolve_audit('insert-only', 'role_id|access_code');
