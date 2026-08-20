package ncasa.household.domain;

import java.time.Instant;
import java.util.Objects;

public final class HouseholdInvitation {
    private final InvitationId id;
    private final HouseholdId householdId;
    private final InvitationEmail email;
    private final HouseholdRole invitedRole;
    private final MemberId invitedBy;
    private final InvitationTokenHash tokenHash;
    private final Instant createdAt;
    private final InvitationExpiry expiry;
    private InvitationStatus status;
    private Instant statusChangedAt;

    private HouseholdInvitation(InvitationId id, HouseholdId householdId, InvitationEmail email,
            HouseholdRole invitedRole, MemberId invitedBy, InvitationTokenHash tokenHash, Instant createdAt,
            InvitationExpiry expiry, InvitationStatus status, Instant statusChangedAt) {
        this.id = Objects.requireNonNull(id);
        this.householdId = Objects.requireNonNull(householdId);
        this.email = Objects.requireNonNull(email);
        this.invitedRole = Objects.requireNonNull(invitedRole);
        this.invitedBy = Objects.requireNonNull(invitedBy);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiry = Objects.requireNonNull(expiry);
        this.status = Objects.requireNonNull(status);
        this.statusChangedAt = Objects.requireNonNull(statusChangedAt);
    }

    public static HouseholdInvitation create(InvitationId id, HouseholdId householdId, InvitationEmail email,
            HouseholdRole invitedRole, MemberId invitedBy, InvitationTokenHash tokenHash, Instant createdAt,
            InvitationExpiry expiry) {
        return new HouseholdInvitation(id, householdId, email, invitedRole, invitedBy, tokenHash, createdAt,
                expiry, InvitationStatus.PENDING, createdAt);
    }

    public static HouseholdInvitation rehydrate(InvitationId id, HouseholdId householdId, InvitationEmail email,
            HouseholdRole invitedRole, MemberId invitedBy, InvitationTokenHash tokenHash, Instant createdAt,
            InvitationExpiry expiry, InvitationStatus status, Instant statusChangedAt) {
        return new HouseholdInvitation(id, householdId, email, invitedRole, invitedBy, tokenHash, createdAt,
                expiry, status, statusChangedAt);
    }

    public void accept(InvitationEmail authenticatedEmail, Instant now) {
        requirePending();
        if (expiry.hasExpired(now)) {
            status = InvitationStatus.EXPIRED;
            statusChangedAt = now;
            throw new InvitationExpiredException();
        }
        if (!email.equals(authenticatedEmail)) throw new HouseholdAccessDeniedException("Invitation belongs to another email");
        status = InvitationStatus.ACCEPTED;
        statusChangedAt = now;
    }

    public void cancel(Instant now) {
        requirePending();
        status = InvitationStatus.CANCELLED;
        statusChangedAt = now;
    }

    public boolean expireIfNeeded(Instant now) {
        if (status == InvitationStatus.PENDING && expiry.hasExpired(now)) {
            status = InvitationStatus.EXPIRED;
            statusChangedAt = now;
            return true;
        }
        return false;
    }

    private void requirePending() {
        if (status != InvitationStatus.PENDING) throw new InvitationStateException(status);
    }

    public InvitationId id() { return id; }
    public HouseholdId householdId() { return householdId; }
    public InvitationEmail email() { return email; }
    public HouseholdRole invitedRole() { return invitedRole; }
    public MemberId invitedBy() { return invitedBy; }
    public InvitationTokenHash tokenHash() { return tokenHash; }
    public Instant createdAt() { return createdAt; }
    public InvitationExpiry expiry() { return expiry; }
    public InvitationStatus status() { return status; }
    public Instant statusChangedAt() { return statusChangedAt; }
}
