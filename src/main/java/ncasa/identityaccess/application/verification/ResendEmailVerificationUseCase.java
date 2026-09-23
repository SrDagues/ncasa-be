package ncasa.identityaccess.application.verification;

import java.time.Clock;
import java.time.Duration;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenRepository;
import ncasa.identityaccess.application.port.out.UserAccountRepository;
import ncasa.identityaccess.domain.Email;

public final class ResendEmailVerificationUseCase {
    private final UserAccountRepository users;
    private final EmailVerificationTokenRepository tokens;
    private final IssueEmailVerification issuer;
    private final Clock clock;
    private final Duration cooldown;
    private final int maxSendsPer24Hours;

    public ResendEmailVerificationUseCase(UserAccountRepository users, EmailVerificationTokenRepository tokens,
            IssueEmailVerification issuer, Clock clock, Duration cooldown, int maxSendsPer24Hours) {
        this.users = users;
        this.tokens = tokens;
        this.issuer = issuer;
        this.clock = clock;
        this.cooldown = cooldown;
        this.maxSendsPer24Hours = maxSendsPer24Hours;
    }

    public void execute(String rawEmail) {
        var candidate = users.findByEmail(Email.of(rawEmail));
        if (candidate.isEmpty()) return;
        var account = users.findByIdForUpdate(candidate.orElseThrow().id()).orElseThrow();
        if (account.isEmailVerified()) return;
        var now = clock.instant();
        if (tokens.findLatestByUserId(account.id())
                .filter(latest -> latest.createdAt().plus(cooldown).isAfter(now)).isPresent()) return;
        if (tokens.countCreatedSince(account.id(), now.minus(Duration.ofHours(24))) >= maxSendsPer24Hours) return;
        issuer.issue(account.id(), account.email(), now);
    }
}
