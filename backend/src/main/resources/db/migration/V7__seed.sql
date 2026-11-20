INSERT INTO access_types (code, description) VALUES
    ('TICKET_CREATE', 'Create tickets'),
    ('TICKET_VIEW', 'View tickets'),
    ('TICKET_UPDATE', 'Update tickets'),
    ('TICKET_STATUS', 'Change ticket status'),
    ('TICKET_COMMENT', 'Comment on tickets'),
    ('TICKET_SEARCH', 'Search tickets'),
    ('TICKET_ASSISTANT', 'Use the assistant'),
    ('USER_MANAGE', 'Manage users'),
    ('ACCESS_MANAGE', 'Manage access')
ON CONFLICT (code) DO NOTHING;

INSERT INTO roles (id, name) VALUES
    ('11111111-1111-4111-8111-111111111111', 'Support agent'),
    ('22222222-2222-4222-8222-222222222222', 'User manager'),
    ('33333333-3333-4333-8333-333333333333', 'Admin')
ON CONFLICT (id) DO NOTHING;

INSERT INTO role_access (role_id, access_code)
SELECT '11111111-1111-4111-8111-111111111111', code
FROM access_types
WHERE code LIKE 'TICKET_%'
ON CONFLICT (role_id, access_code) DO NOTHING;

INSERT INTO role_access (role_id, access_code) VALUES
    ('22222222-2222-4222-8222-222222222222', 'USER_MANAGE'),
    ('33333333-3333-4333-8333-333333333333', 'USER_MANAGE'),
    ('33333333-3333-4333-8333-333333333333', 'ACCESS_MANAGE')
ON CONFLICT (role_id, access_code) DO NOTHING;

INSERT INTO groups (id, name, active) VALUES
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1', 'Support', true),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2', 'User managers', true),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa3', 'Administrators', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO group_role (group_id, role_id) VALUES
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1', '11111111-1111-4111-8111-111111111111'),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2', '22222222-2222-4222-8222-222222222222'),
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa3', '33333333-3333-4333-8333-333333333333')
ON CONFLICT (group_id, role_id) DO NOTHING;
