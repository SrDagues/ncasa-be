CREATE UNIQUE INDEX uk_household_active_owner
    ON household_members(household_id)
    WHERE is_owner = TRUE AND status = 'ACTIVE';

CREATE UNIQUE INDEX uk_household_pending_invitation
    ON household_invitations(household_id, lower(email))
    WHERE status = 'PENDING';
