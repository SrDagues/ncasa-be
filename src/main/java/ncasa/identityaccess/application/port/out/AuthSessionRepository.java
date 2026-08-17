package ncasa.identityaccess.application.port.out;

import java.util.Optional;
import ncasa.identityaccess.domain.AuthSession;
import ncasa.identityaccess.domain.RefreshTokenHash;
import ncasa.identityaccess.domain.UserId;

public interface AuthSessionRepository {
    AuthSession save(AuthSession session);
    Optional<AuthSession> findByTokenHashForUpdate(RefreshTokenHash tokenHash);
    void revokeAllByUser(UserId userId);
}
