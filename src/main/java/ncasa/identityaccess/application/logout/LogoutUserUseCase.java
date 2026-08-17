package ncasa.identityaccess.application.logout;

import ncasa.identityaccess.application.InvalidRefreshTokenException;
import ncasa.identityaccess.application.port.out.AuthSessionRepository;
import ncasa.identityaccess.application.port.out.TokenHasher;
import ncasa.identityaccess.domain.UserId;

public final class LogoutUserUseCase {
    private final AuthSessionRepository sessions;
    private final TokenHasher tokenHasher;

    public LogoutUserUseCase(AuthSessionRepository sessions, TokenHasher tokenHasher) {
        this.sessions = sessions;
        this.tokenHasher = tokenHasher;
    }

    public void execute(String rawRefreshToken, Long authenticatedUserId) {
        var session = sessions.findByTokenHashForUpdate(tokenHasher.hash(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (session.revoked() || !session.userId().equals(new UserId(authenticatedUserId))) {
            throw new InvalidRefreshTokenException();
        }
        session.revoke();
        sessions.save(session);
    }
}
