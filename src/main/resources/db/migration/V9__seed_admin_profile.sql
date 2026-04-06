INSERT INTO profiles (user_id, display_name, type, is_active, created_at, updated_at)
VALUES ((SELECT id FROM users WHERE email = 'admin@inspectpro.com'),
        'Admin',
        'BASIC',
        true,
        NOW(),
        NOW());

INSERT INTO subscriptions (user_id, tier, is_active, started_at, created_at, updated_at)
VALUES ((SELECT id FROM users WHERE email = 'admin@inspectpro.com'),
        'BASIC',
        true,
        NOW(),
        NOW(),
        NOW());