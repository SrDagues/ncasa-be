package ncasa.identityaccess.application.verification;

import java.time.Instant;
import java.util.UUID;
import ncasa.identityaccess.domain.Email;

public record EmailVerificationRequested(UUID requestId, Email recipient, String rawToken, Instant expiresAt) {}
