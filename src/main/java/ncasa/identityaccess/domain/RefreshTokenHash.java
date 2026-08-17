package ncasa.identityaccess.domain;

import java.util.Objects;

public record RefreshTokenHash(String value) {
    public RefreshTokenHash {
        Objects.requireNonNull(value, "RefreshTokenHash is required");
        if (value.isBlank()) throw new IllegalArgumentException("RefreshTokenHash cannot be blank");
    }
}
