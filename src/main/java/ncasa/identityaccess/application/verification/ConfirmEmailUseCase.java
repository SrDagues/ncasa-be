package ncasa.identityaccess.application.verification;

import java.time.Clock;
import ncasa.identityaccess.application.ExpiredEmailVerificationTokenException;
import ncasa.identityaccess.application.InvalidEmailVerificationTokenException;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenHasher;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenRepository;
import ncasa.identityaccess.application.port.out.UserAccountRepository;

public final class ConfirmEmailUseCase {
    private final UserAccountRepository users;
    private final EmailVerificationTokenRepository tokens;
    private final EmailVerificationTokenHasher hasher;
    private final Clock clock;

    public ConfirmEmailUseCase(UserAccountRepository users, EmailVerificationTokenRepository tokens,
            EmailVerificationTokenHasher hasher, Clock clock) {
        this.users = users;
        this.tokens = tokens;
        this.hasher = hasher;
        this.clock = clock;
    }

    public void execute(String rawToken) {
        var hash = hasher.hash(rawToken);
        var initial = tokens.findByHash(hash).orElseThrow(InvalidEmailVerificationTokenException::new);
        var account = users.findByIdForUpdate(initial.userId())
                .orElseThrow(InvalidEmailVerificationTokenException::new);
        var token = tokens.findByHash(hash).orElseThrow(InvalidEmailVerificationTokenException::new);
        var now = clock.instant();
        if (token.consumedAt() != null || token.invalidatedAt() != null) {
            throw new InvalidEmailVerificationTokenException();
        }
        if (token.hasExpired(now)) throw new ExpiredEmailVerificationTokenException();
        account.confirmEmail(now);
        var allTokens = tokens.findByUserId(account.id());
        allTokens.forEach(candidate -> {
            if (candidate.id().equals(token.id())) candidate.consume(now);
            else candidate.invalidate(now);
        });
        users.save(account);
        tokens.saveAll(allTokens);
    }
}
