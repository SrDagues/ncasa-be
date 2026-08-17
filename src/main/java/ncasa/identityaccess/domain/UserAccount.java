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
    private final Instant updatedAt;

    private UserAccount(UserId id, Email email, PasswordHash passwordHash, AccountStatus status,
            Set<GlobalRole> roles, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.roles = Set.copyOf(roles);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserAccount registered(Email email, PasswordHash passwordHash, Instant now) {
        return new UserAccount(null, email, passwordHash, AccountStatus.ACTIVE,
                Set.of(GlobalRole.ROLE_USER), now, now);
    }

    public static UserAccount rehydrate(UserId id, Email email, PasswordHash passwordHash,
            AccountStatus status, Set<GlobalRole> roles, Instant createdAt, Instant updatedAt) {
        return new UserAccount(id, email, passwordHash, status, roles, createdAt, updatedAt);
    }

    public UserAccount withId(UserId assignedId) {
        return new UserAccount(assignedId, email, passwordHash, status, roles, createdAt, updatedAt);
    }

    public boolean canAuthenticate() { return status == AccountStatus.ACTIVE; }
    public UserId id() { return id; }
    public Email email() { return email; }
    public PasswordHash passwordHash() { return passwordHash; }
    public AccountStatus status() { return status; }
    public Set<GlobalRole> roles() { return roles; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
