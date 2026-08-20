package ncasa.household.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HouseholdInvitationTest {
    private static final Instant NOW = Instant.parse("2026-08-20T10:00:00Z");

    @Test
    void shouldAcceptPendingInvitationForMatchingEmail() {
        HouseholdInvitation invitation = invitation();

        invitation.accept(InvitationEmail.of("user@example.com"), NOW.plusSeconds(1));

        assertThat(invitation.status()).isEqualTo(InvitationStatus.ACCEPTED);
    }

    @Test
    void shouldRejectWrongEmailAndExpiredInvitation() {
        HouseholdInvitation invitation = invitation();
        assertThatThrownBy(() -> invitation.accept(InvitationEmail.of("other@example.com"), NOW.plusSeconds(1)))
                .isInstanceOf(HouseholdAccessDeniedException.class);

        assertThatThrownBy(() -> invitation.accept(InvitationEmail.of("user@example.com"), NOW.plus(Duration.ofDays(8))))
                .isInstanceOf(InvitationExpiredException.class);
        assertThat(invitation.status()).isEqualTo(InvitationStatus.EXPIRED);
    }

    @Test
    void shouldRejectTransitionsFromTerminalState() {
        HouseholdInvitation invitation = invitation();
        invitation.cancel(NOW.plusSeconds(1));

        assertThatThrownBy(() -> invitation.accept(InvitationEmail.of("user@example.com"), NOW.plusSeconds(2)))
                .isInstanceOf(InvitationStateException.class);
        assertThatThrownBy(() -> invitation.cancel(NOW.plusSeconds(2)))
                .isInstanceOf(InvitationStateException.class);
    }

    private HouseholdInvitation invitation() {
        return HouseholdInvitation.create(new InvitationId(UUID.randomUUID()), new HouseholdId(UUID.randomUUID()),
                InvitationEmail.of("user@example.com"), HouseholdRole.MEMBER, new MemberId(UUID.randomUUID()),
                new InvitationTokenHash("a".repeat(64)), NOW, InvitationExpiry.after(NOW, Duration.ofDays(7)));
    }
}
