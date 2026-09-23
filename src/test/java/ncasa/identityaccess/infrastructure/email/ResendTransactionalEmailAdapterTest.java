package ncasa.identityaccess.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import ncasa.identityaccess.application.port.out.TransactionalEmail;
import ncasa.identityaccess.infrastructure.config.ResendProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

class ResendTransactionalEmailAdapterTest {
    @Test
    void shouldSelectTheProductionConstructorWhenCreatedBySpring() {
        new ApplicationContextRunner().withUserConfiguration(TestConfiguration.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(ResendTransactionalEmailAdapter.class);
        });
    }

    @Test
    void shouldSendHtmlAndTextWithAuthorizationAndIdempotency() {
        var fixture = fixture(1);
        fixture.server.expect(once(), requestTo("https://api.resend.test/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer secret-test-key"))
                .andExpect(header("Idempotency-Key", "email-verification/request-1"))
                .andExpect(jsonPath("$.from").value("nCasa <no-reply@mail.ncasa.es>"))
                .andExpect(jsonPath("$.html").value("<strong>Confirma</strong>"))
                .andExpect(jsonPath("$.text").value("Confirma en texto"))
                .andExpect(jsonPath("$.tags[0].name").value("category"))
                .andRespond(withSuccess("{\"id\":\"49a3999c-0ce1-4ea6-ab68-afcd6dc2e794\"}", MediaType.APPLICATION_JSON));

        fixture.adapter.send(message());

        fixture.server.verify();
    }

    @Test
    void shouldNotRetryDailyQuotaErrors() {
        var fixture = fixture(3);
        fixture.server.expect(once(), requestTo("https://api.resend.test/emails"))
                .andRespond(withTooManyRequests().body("{\"name\":\"daily_quota_exceeded\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> fixture.adapter.send(message())).isInstanceOf(ResendDeliveryException.class);
        fixture.server.verify();
    }

    private Fixture fixture(int attempts) {
        var properties = new ResendProperties("secret-test-key", "nCasa <no-reply@mail.ncasa.es>",
                URI.create("https://api.resend.test"), Duration.ofSeconds(1), Duration.ofSeconds(1), attempts);
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.apiUrl().toString());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new Fixture(new ResendTransactionalEmailAdapter(new ObjectMapper(), properties, builder.build()), server);
    }

    private TransactionalEmail message() {
        return new TransactionalEmail("user@example.com", "Confirma", "<strong>Confirma</strong>",
                "Confirma en texto", "email-verification/request-1", Map.of("category", "email-verification"));
    }

    private record Fixture(ResendTransactionalEmailAdapter adapter, MockRestServiceServer server) {}

    @Configuration(proxyBeanMethods = false)
    @Import(ResendTransactionalEmailAdapter.class)
    static class TestConfiguration {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        ResendProperties resendProperties() {
            return new ResendProperties("secret-test-key", "nCasa <no-reply@mail.ncasa.es>",
                    URI.create("https://api.resend.test"), Duration.ofSeconds(1), Duration.ofSeconds(1), 1);
        }
    }
}
