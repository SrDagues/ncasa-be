package ncasa.identityaccess.infrastructure.web;

import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {
    private final RefreshCookieProperties properties;

    public RefreshTokenCookieFactory(RefreshCookieProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie create(String refreshToken) {
        return cookie(refreshToken, properties.maxAge());
    }

    public ResponseCookie expire() {
        return cookie("", Duration.ZERO);
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(properties.name(), value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(properties.path())
                .maxAge(maxAge)
                .build();
    }
}
