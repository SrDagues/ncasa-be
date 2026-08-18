package ncasa.identityaccess.infrastructure.web;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.auth.refresh-cookie")
public record RefreshCookieProperties(
        String name,
        String path,
        String sameSite,
        boolean secure,
        Duration maxAge) {}
