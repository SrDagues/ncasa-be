package ncasa.household.infrastructure.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ncasa.household.invitation")
public record HouseholdInvitationProperties(Duration expiration, URI frontendUrl, String from) {
    public HouseholdInvitationProperties {
        if (expiration == null || expiration.isNegative() || expiration.isZero()) throw new IllegalArgumentException("Invitation expiration must be positive");
        if (frontendUrl == null) throw new IllegalArgumentException("Invitation frontend URL is required");
        if (from == null || from.isBlank()) throw new IllegalArgumentException("Invitation sender is required");
    }
}
