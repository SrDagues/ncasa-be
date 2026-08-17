package ncasa.identityaccess.infrastructure.persistence;

import java.util.Optional;
import ncasa.identityaccess.application.port.out.AuthSessionRepository;
import ncasa.identityaccess.domain.AuthSession;
import ncasa.identityaccess.domain.RefreshTokenHash;
import ncasa.identityaccess.domain.UserId;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAuthSessionRepositoryAdapter implements AuthSessionRepository {
    private final SpringDataAuthSessionRepository sessions;

    public JpaAuthSessionRepositoryAdapter(SpringDataAuthSessionRepository sessions) {
        this.sessions = sessions;
    }

    @Override
    public AuthSession save(AuthSession session) {
        var saved = sessions.save(new JpaAuthSessionEntity(session.id(), session.tokenHash().value(),
                session.userId().value(), session.expiresAt(), session.revoked(), session.createdAt(),
                session.replacedById()));
        return toDomain(saved);
    }

    @Override
    public Optional<AuthSession> findByTokenHashForUpdate(RefreshTokenHash tokenHash) {
        return sessions.findByTokenHash(tokenHash.value()).map(this::toDomain);
    }

    @Override
    public void revokeAllByUser(UserId userId) {
        sessions.findAllByUserIdAndRevokedFalse(userId.value()).forEach(entity -> {
            var domain = toDomain(entity);
            domain.revoke();
            save(domain);
        });
    }

    private AuthSession toDomain(JpaAuthSessionEntity entity) {
        return AuthSession.rehydrate(entity.id(), new UserId(entity.userId()),
                new RefreshTokenHash(entity.tokenHash()), entity.expiresAt(), entity.createdAt(),
                entity.revoked(), entity.replacedByTokenId());
    }
}
