package ncasa.identityaccess.application.session;

import java.time.Clock;
import java.time.Duration;
import ncasa.identityaccess.application.AuthenticatedUser;
import ncasa.identityaccess.application.AuthenticationResult;
import ncasa.identityaccess.application.port.out.AccessTokenIssuer;
import ncasa.identityaccess.application.port.out.AuthSessionRepository;
import ncasa.identityaccess.application.port.out.RefreshTokenGenerator;
import ncasa.identityaccess.application.port.out.TokenHasher;
import ncasa.identityaccess.domain.AuthSession;
import ncasa.identityaccess.domain.UserAccount;

public final class SessionIssuer {
    private final AuthSessionRepository sessions;
    private final RefreshTokenGenerator refreshTokens;
    private final TokenHasher tokenHasher;
    private final AccessTokenIssuer accessTokens;
    private final Clock clock;
    private final Duration refreshTokenLifetime;

    public SessionIssuer(AuthSessionRepository sessions, RefreshTokenGenerator refreshTokens, TokenHasher tokenHasher,
            AccessTokenIssuer accessTokens, Clock clock, Duration refreshTokenLifetime) {
        this.sessions = sessions;
        this.refreshTokens = refreshTokens;
        this.tokenHasher = tokenHasher;
        this.accessTokens = accessTokens;
        this.clock = clock;
        this.refreshTokenLifetime = refreshTokenLifetime;
    }

    public IssuedAuthentication issue(UserAccount account) {
        String rawRefreshToken = refreshTokens.generate();
        var now = clock.instant();
        AuthSession session = sessions.save(AuthSession.create(account.id(), tokenHasher.hash(rawRefreshToken),
                now.plus(refreshTokenLifetime), now));
        var access = accessTokens.issue(account);
        var user = new AuthenticatedUser(account.id().value(), account.email().value(),
                account.roles().stream().map(Enum::name).sorted().toList());
        return new IssuedAuthentication(
                new AuthenticationResult(access.value(), rawRefreshToken, "Bearer", access.expiresInSeconds(), user),
                session);
    }

    public record IssuedAuthentication(AuthenticationResult result, AuthSession session) {}
}
