package ncasa.identityaccess.application.logout;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import ncasa.identityaccess.application.port.out.AuthSessionRepository;
import ncasa.identityaccess.domain.AuthSession;
import ncasa.identityaccess.domain.RefreshTokenHash;
import ncasa.identityaccess.domain.UserId;
import org.junit.jupiter.api.Test;

class LogoutUserUseCaseTest {

    @Test
    void shouldRevokeSessionWhenRefreshTokenExists() {
        AuthSession session = AuthSession.create(
                new UserId(1L), new RefreshTokenHash("hashed-token"),
                Instant.parse("2026-09-01T00:00:00Z"), Instant.parse("2026-08-01T00:00:00Z"));
        var sessions = new FakeAuthSessionRepository(session);
        var useCase = new LogoutUserUseCase(sessions, raw -> new RefreshTokenHash("hashed-" + raw));

        useCase.execute("token");

        assertThat(session.revoked()).isTrue();
        assertThat(sessions.saved).isSameAs(session);
    }

    @Test
    void shouldDoNothingWhenRefreshTokenDoesNotExist() {
        var sessions = new FakeAuthSessionRepository(null);
        var useCase = new LogoutUserUseCase(sessions, raw -> new RefreshTokenHash("hashed-" + raw));

        useCase.execute("missing");

        assertThat(sessions.saved).isNull();
    }

    private static final class FakeAuthSessionRepository implements AuthSessionRepository {
        private final AuthSession found;
        private AuthSession saved;

        private FakeAuthSessionRepository(AuthSession found) {
            this.found = found;
        }

        @Override
        public AuthSession save(AuthSession session) {
            this.saved = session;
            return session;
        }

        @Override
        public Optional<AuthSession> findByTokenHashForUpdate(RefreshTokenHash tokenHash) {
            return Optional.ofNullable(found)
                    .filter(session -> session.tokenHash().equals(tokenHash));
        }

        @Override
        public void revokeAllByUser(UserId userId) {}
    }
}
