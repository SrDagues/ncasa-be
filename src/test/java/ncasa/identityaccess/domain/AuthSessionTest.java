package ncasa.identityaccess.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuthSessionTest {
    @Test
    void shouldBeUsableBeforeExpirationWhenNotRevoked() {
        Instant now = Instant.parse("2026-08-17T10:00:00Z");
        var session = AuthSession.create(new UserId(1L), new RefreshTokenHash("hash"), now.plusSeconds(60), now);
        assertThat(session.isUsableAt(now.plusSeconds(30))).isTrue();
    }

    @Test
    void shouldNotBeUsableAfterRevocation() {
        Instant now = Instant.parse("2026-08-17T10:00:00Z");
        var session = AuthSession.create(new UserId(1L), new RefreshTokenHash("hash"), now.plusSeconds(60), now);
        session.revoke();
        assertThat(session.isUsableAt(now.plusSeconds(10))).isFalse();
    }
}
