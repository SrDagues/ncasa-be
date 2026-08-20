CREATE TABLE households (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    owner_member_id UUID NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_households_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE TABLE household_members (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL REFERENCES households(id),
    account_id BIGINT NOT NULL REFERENCES users(id),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    is_owner BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status_changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_household_member_account UNIQUE (household_id, account_id),
    CONSTRAINT ck_household_member_role CHECK (role IN ('ADMIN', 'MEMBER')),
    CONSTRAINT ck_household_member_status CHECK (status IN ('ACTIVE', 'LEFT', 'REMOVED'))
);
CREATE INDEX idx_household_members_account ON household_members(account_id);

CREATE TABLE household_invitations (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL REFERENCES households(id),
    email VARCHAR(320) NOT NULL,
    invited_role VARCHAR(20) NOT NULL,
    invited_by UUID NOT NULL REFERENCES household_members(id),
    token_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status_changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_household_invitation_token UNIQUE (token_hash),
    CONSTRAINT ck_household_invitation_role CHECK (invited_role IN ('ADMIN', 'MEMBER')),
    CONSTRAINT ck_household_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED', 'CANCELLED', 'EXPIRED'))
);
CREATE INDEX idx_household_invitations_email ON household_invitations(email);
