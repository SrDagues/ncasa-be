package ncasa.identityaccess.domain;

import java.util.Objects;

public record EmailVerificationTokenHash(String value) {
    public EmailVerificationTokenHash {
        Objects.requireNonNull(value, "Email verification token hash is required");
        if (!value.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Email verification token hash must be a SHA-256 digest");
        }
    }
}
