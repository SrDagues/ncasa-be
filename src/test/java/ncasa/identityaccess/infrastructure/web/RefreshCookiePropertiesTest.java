package ncasa.identityaccess.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class RefreshCookiePropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                    "app.auth.refresh-cookie.name=ncasa_refresh",
                    "app.auth.refresh-cookie.path=/api/auth",
                    "app.auth.refresh-cookie.same-site=Lax",
                    "app.auth.refresh-cookie.secure=false",
                    "app.auth.refresh-cookie.max-age=30d");

    @Test
    void shouldLoadRefreshCookieConfiguration() {
        contextRunner.run(context -> {
            RefreshCookieProperties properties = context.getBean(RefreshCookieProperties.class);

            assertThat(properties.name()).isEqualTo("ncasa_refresh");
            assertThat(properties.path()).isEqualTo("/api/auth");
            assertThat(properties.sameSite()).isEqualTo("Lax");
            assertThat(properties.secure()).isFalse();
            assertThat(properties.maxAge()).isEqualTo(Duration.ofDays(30));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(RefreshCookieProperties.class)
    static class TestConfiguration {}
}
