package ncasa.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, Duration accessTokenExpiration, Duration refreshTokenExpiration) {
    public JwtProperties {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("security.jwt.secret/JWT_SECRET must contain at least 32 characters");
        }
        if (accessTokenExpiration == null || accessTokenExpiration.isNegative() || accessTokenExpiration.isZero()) {
            throw new IllegalArgumentException("security.jwt.access-token-expiration must be positive");
        }
        if (refreshTokenExpiration == null || refreshTokenExpiration.isNegative() || refreshTokenExpiration.isZero()) {
            throw new IllegalArgumentException("security.jwt.refresh-token-expiration must be positive");
        }
    }
}
