CREATE TYPE credential_type AS ENUM (
    'HVAC_LICENSE',
    'EPA_CERTIFICATION',
    'INSURANCE',
    'STATE_LICENSE'
);

CREATE TYPE credential_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'EXPIRED'
);

CREATE TABLE credentials (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profiles(id),
    type credential_type NOT NULL,
    issuer VARCHAR(255) NOT NULL,
    license_number VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    status credential_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_credentials_profile_id ON credentials(profile_id);
CREATE INDEX idx_credentials_status ON credentials(status);
CREATE INDEX idx_credentials_expiry_date ON credentials(expiry_date);