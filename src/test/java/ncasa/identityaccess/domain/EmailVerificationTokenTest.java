package ncasa.identityaccess.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmailVerificationTokenTest {
    private static final Instant NOW = Instant.parse("2026-09-20T10:00:00Z");

    @Test
    void shouldBeUsableOnlyBeforeExpiryAndUntilConsumed() {
        var token = token();
        assertThat(token.isUsableAt(NOW.plusSeconds(30))).isTrue();

        token.consume(NOW.plusSeconds(30));

        assertThat(token.isUsableAt(NOW.plusSeconds(31))).isFalse();
        assertThatThrownBy(() -> token.consume(NOW.plusSeconds(32))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldTreatExactExpiryAsExpiredAndAllowInvalidationOnce() {
        var token = token();
        assertThat(token.hasExpired(NOW.plusSeconds(60))).isTrue();

        token.invalidate(NOW.plusSeconds(10));
        token.invalidate(NOW.plusSeconds(20));

        assertThat(token.invalidatedAt()).isEqualTo(NOW.plusSeconds(10));
    }

    private EmailVerificationToken token() {
        return EmailVerificationToken.create(UUID.randomUUID(), new UserId(1L),
                new EmailVerificationTokenHash("a".repeat(64)), NOW, NOW.plusSeconds(60));
    }
}
