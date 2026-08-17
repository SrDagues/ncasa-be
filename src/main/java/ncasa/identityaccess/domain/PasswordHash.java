package ncasa.identityaccess.domain;

import java.util.Objects;

public record PasswordHash(String value) {
    public PasswordHash {
        Objects.requireNonNull(value, "PasswordHash is required");
        if (value.isBlank()) throw new IllegalArgumentException("PasswordHash cannot be blank");
    }
}
