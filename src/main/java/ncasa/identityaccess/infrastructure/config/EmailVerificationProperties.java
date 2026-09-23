package ncasa.identityaccess.infrastructure.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ncasa.identity-access.email-verification")
public record EmailVerificationProperties(Duration tokenTtl, Duration resendCooldown, int maxSendsPer24h,
        Duration tokenRetention, int resendRatePerMinute, int confirmRatePerMinute, URI frontendUrl) {
    public EmailVerificationProperties {
        positive(tokenTtl, "Token TTL");
        positive(resendCooldown, "Resend cooldown");
        positive(tokenRetention, "Token retention");
        if (maxSendsPer24h < 1 || resendRatePerMinute < 1 || confirmRatePerMinute < 1) {
            throw new IllegalArgumentException("Email verification limits must be positive");
        }
        if (frontendUrl == null || frontendUrl.getScheme() == null
                || !(frontendUrl.getScheme().equals("http") || frontendUrl.getScheme().equals("https"))) {
            throw new IllegalArgumentException("Frontend URL must be an absolute HTTP(S) URI");
        }
    }

    private static void positive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
