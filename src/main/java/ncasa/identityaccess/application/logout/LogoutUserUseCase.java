package ncasa.identityaccess.application.logout;

import ncasa.identityaccess.application.port.out.AuthSessionRepository;
import ncasa.identityaccess.application.port.out.TokenHasher;

public final class LogoutUserUseCase {
    private final AuthSessionRepository sessions;
    private final TokenHasher tokenHasher;

    public LogoutUserUseCase(AuthSessionRepository sessions, TokenHasher tokenHasher) {
        this.sessions = sessions;
        this.tokenHasher = tokenHasher;
    }

    public void execute(String rawRefreshToken) {
        sessions.findByTokenHashForUpdate(tokenHasher.hash(rawRefreshToken))
                .filter(session -> !session.revoked())
                .ifPresent(session -> {
                    session.revoke();
                    sessions.save(session);
                });
    }
}
