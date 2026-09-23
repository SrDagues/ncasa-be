ALTER TABLE users ADD COLUMN email_verified_at TIMESTAMP WITH TIME ZONE;
UPDATE users SET email_verified_at = created_at;

CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
    invalidated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_email_verification_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_email_verification_token_expiry CHECK (expires_at > created_at),
    CONSTRAINT ck_email_verification_token_terminal CHECK (consumed_at IS NULL OR invalidated_at IS NULL)
);

CREATE INDEX idx_email_verification_tokens_user_created
    ON email_verification_tokens(user_id, created_at DESC);
