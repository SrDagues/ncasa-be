package ncasa.identityaccess.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RefreshTokenCookieFactoryTest {

    private final RefreshCookieProperties properties = new RefreshCookieProperties(
            "ncasa_refresh", "/api/auth", "Lax", false, Duration.ofDays(30));
    private final RefreshTokenCookieFactory factory = new RefreshTokenCookieFactory(properties);

    @Test
    void shouldCreateHttpOnlyRefreshCookie() {
        var cookie = factory.create("refresh-token");

        assertThat(cookie.getName()).isEqualTo("ncasa_refresh");
        assertThat(cookie.getValue()).isEqualTo("refresh-token");
        assertThat(cookie.getPath()).isEqualTo("/api/auth");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isFalse();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(30));
    }

    @Test
    void shouldCreateExpiredCookieForLogout() {
        var cookie = factory.expire();

        assertThat(cookie.getName()).isEqualTo("ncasa_refresh");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getPath()).isEqualTo("/api/auth");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isZero();
    }
}
