package ncasa.identityaccess.application.refresh;

import java.time.Clock;
import ncasa.identityaccess.application.AuthenticationResult;
import ncasa.identityaccess.application.InvalidRefreshTokenException;
import ncasa.identityaccess.application.port.out.AuthSessionRepository;
import ncasa.identityaccess.application.port.out.TokenHasher;
import ncasa.identityaccess.application.port.out.UserAccountRepository;
import ncasa.identityaccess.application.session.SessionIssuer;

public final class RefreshSessionUseCase {
    private final AuthSessionRepository sessions;
    private final UserAccountRepository users;
    private final TokenHasher tokenHasher;
    private final SessionIssuer sessionIssuer;
    private final Clock clock;

    public RefreshSessionUseCase(AuthSessionRepository sessions, UserAccountRepository users, TokenHasher tokenHasher,
            SessionIssuer sessionIssuer, Clock clock) {
        this.sessions = sessions;
        this.users = users;
        this.tokenHasher = tokenHasher;
        this.sessionIssuer = sessionIssuer;
        this.clock = clock;
    }

    public AuthenticationResult execute(String rawRefreshToken) {
        var current = sessions.findByTokenHashForUpdate(tokenHasher.hash(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (!current.isUsableAt(clock.instant())) throw new InvalidRefreshTokenException();
        var account = users.findById(current.userId()).orElseThrow(InvalidRefreshTokenException::new);
        if (!account.canAuthenticate()) throw new InvalidRefreshTokenException();

        var replacement = sessionIssuer.issue(account);
        current.revoke(replacement.session().id());
        sessions.save(current);
        return replacement.result();
    }
}
