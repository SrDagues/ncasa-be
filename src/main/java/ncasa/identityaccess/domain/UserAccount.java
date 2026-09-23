package ncasa.identityaccess.domain;

import java.time.Instant;
import java.util.Set;

public final class UserAccount {
    private final UserId id;
    private final Email email;
    private final PasswordHash passwordHash;
    private final AccountStatus status;
    private final Set<GlobalRole> roles;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant emailVerifiedAt;

    private UserAccount(UserId id, Email email, PasswordHash passwordHash, AccountStatus status,
            Set<GlobalRole> roles, Instant createdAt, Instant updatedAt, Instant emailVerifiedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.roles = Set.copyOf(roles);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public static UserAccount registered(Email email, PasswordHash passwordHash, Instant now) {
        return new UserAccount(null, email, passwordHash, AccountStatus.ACTIVE,
                Set.of(GlobalRole.ROLE_USER), now, now, null);
    }

    public static UserAccount rehydrate(UserId id, Email email, PasswordHash passwordHash,
            AccountStatus status, Set<GlobalRole> roles, Instant createdAt, Instant updatedAt,
            Instant emailVerifiedAt) {
        return new UserAccount(id, email, passwordHash, status, roles, createdAt, updatedAt, emailVerifiedAt);
    }

    public UserAccount withId(UserId assignedId) {
        return new UserAccount(assignedId, email, passwordHash, status, roles, createdAt, updatedAt, emailVerifiedAt);
    }

    public void confirmEmail(Instant now) {
        if (emailVerifiedAt != null) throw new EmailAlreadyVerifiedException();
        emailVerifiedAt = java.util.Objects.requireNonNull(now, "Verification time is required");
        updatedAt = now;
    }

    public boolean isEmailVerified() { return emailVerifiedAt != null; }
    public boolean canAuthenticate() { return status == AccountStatus.ACTIVE && isEmailVerified(); }
    public UserId id() { return id; }
    public Email email() { return email; }
    public PasswordHash passwordHash() { return passwordHash; }
    public AccountStatus status() { return status; }
    public Set<GlobalRole> roles() { return roles; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant emailVerifiedAt() { return emailVerifiedAt; }
}
