package ncasa.household.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HouseholdValueObjectsTest {
    @Test
    void shouldNormalizeHouseholdNameAndInvitationEmail() {
        assertThat(HouseholdName.of("  Casa   Azul  ").value()).isEqualTo("Casa Azul");
        assertThat(InvitationEmail.of(" USER@Example.com ").value()).isEqualTo("user@example.com");
    }

    @Test
    void shouldRejectInvalidIdentifiersAndValues() {
        assertThatThrownBy(() -> new HouseholdId(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MemberId(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AccountId(0L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HouseholdName.of(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InvitationEmail.of("invalid")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBuildAnExpiryAfterCreation() {
        Instant createdAt = Instant.parse("2026-08-20T10:00:00Z");
        var expiry = InvitationExpiry.after(createdAt, Duration.ofDays(7));

        assertThat(expiry.value()).isEqualTo(Instant.parse("2026-08-27T10:00:00Z"));
        assertThat(expiry.hasExpired(Instant.parse("2026-08-27T10:00:01Z"))).isTrue();
        assertThatThrownBy(() -> new InvitationExpiry(createdAt, createdAt))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
