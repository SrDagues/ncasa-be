package ncasa.household.domain;

import java.time.Duration;
import java.time.Instant;

public record InvitationExpiry(Instant createdAt, Instant value) {
    public InvitationExpiry {
        if (createdAt == null || value == null || !value.isAfter(createdAt)) {
            throw new IllegalArgumentException("Invitation expiry must be after creation");
        }
    }

    public static InvitationExpiry after(Instant createdAt, Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Invitation duration must be positive");
        }
        return new InvitationExpiry(createdAt, createdAt.plus(duration));
    }

    public boolean hasExpired(Instant now) {
        return !now.isBefore(value);
    }
}
