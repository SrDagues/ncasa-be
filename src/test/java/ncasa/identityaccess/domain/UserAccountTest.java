package ncasa.identityaccess.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserAccountTest {
    private static final Instant NOW = Instant.parse("2026-09-20T10:00:00Z");

    @Test
    void shouldRequireEmailVerificationBeforeAuthentication() {
        var account = UserAccount.registered(Email.of("user@example.com"), new PasswordHash("hash"), NOW);

        assertThat(account.isEmailVerified()).isFalse();
        assertThat(account.canAuthenticate()).isFalse();

        account.confirmEmail(NOW.plusSeconds(10));

        assertThat(account.isEmailVerified()).isTrue();
        assertThat(account.canAuthenticate()).isTrue();
        assertThat(account.updatedAt()).isEqualTo(NOW.plusSeconds(10));
    }

    @Test
    void shouldNotConfirmEmailTwice() {
        var account = UserAccount.registered(Email.of("user@example.com"), new PasswordHash("hash"), NOW);
        account.confirmEmail(NOW.plusSeconds(1));

        assertThatThrownBy(() -> account.confirmEmail(NOW.plusSeconds(2)))
                .isInstanceOf(EmailAlreadyVerifiedException.class);
    }
}
