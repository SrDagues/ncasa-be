package ncasa.identityaccess.application.verification;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import ncasa.identityaccess.application.port.out.EmailVerificationEventPublisher;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenGenerator;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenHasher;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenRepository;
import ncasa.identityaccess.domain.Email;
import ncasa.identityaccess.domain.EmailVerificationToken;
import ncasa.identityaccess.domain.UserId;

public final class IssueEmailVerification {
    private final EmailVerificationTokenRepository tokens;
    private final EmailVerificationTokenGenerator generator;
    private final EmailVerificationTokenHasher hasher;
    private final EmailVerificationEventPublisher events;
    private final Duration tokenTtl;

    public IssueEmailVerification(EmailVerificationTokenRepository tokens, EmailVerificationTokenGenerator generator,
            EmailVerificationTokenHasher hasher, EmailVerificationEventPublisher events, Duration tokenTtl) {
        this.tokens = tokens;
        this.generator = generator;
        this.hasher = hasher;
        this.events = events;
        this.tokenTtl = tokenTtl;
    }

    public void issue(UserId userId, Email email, Instant now) {
        String rawToken = generator.generate();
        UUID requestId = UUID.randomUUID();
        Instant expiresAt = now.plus(tokenTtl);
        tokens.save(EmailVerificationToken.create(requestId, userId, hasher.hash(rawToken), now, expiresAt));
        events.publish(new EmailVerificationRequested(requestId, email, rawToken, expiresAt));
    }
}
