CREATE TYPE profile_type AS ENUM ('BASIC', 'VERIFIED_PROFESSIONAL');

CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    display_name VARCHAR(100) NOT NULL,
    type profile_type NOT NULL DEFAULT 'BASIC',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
)

CREATE INDEX idx_profiles_user_id ON profiles(user_id);