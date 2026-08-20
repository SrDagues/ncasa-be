package ncasa.common.infrastructure.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.logging.logback.StructuredLogEncoder;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;
import tools.jackson.databind.ObjectMapper;

class HttpRequestLoggingFilterTest {
    private final HttpRequestLoggingFilter filter = new HttpRequestLoggingFilter();
    private final Logger logger = (Logger) LoggerFactory.getLogger(HttpRequestLoggingFilter.class);
    private CapturingAppender appender;

    @BeforeEach
    void captureLogs() {
        appender = new CapturingAppender();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void cleanUp() {
        logger.detachAppender(appender);
        MDC.clear();
    }

    @Test
    void shouldReuseValidRequestIdAndLogRequestOutcome() throws Exception {
        var request = request("GET", "/api/households/42");
        request.addHeader(HttpRequestLoggingFilter.REQUEST_ID_HEADER, "client-request_42");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            servletRequest.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/households/{id}");
            ((MockHttpServletResponse) servletResponse).setStatus(204);
            assertThat(MDC.get(HttpRequestLoggingFilter.REQUEST_ID_MDC_KEY)).isEqualTo("client-request_42");
        });

        assertThat(response.getHeader(HttpRequestLoggingFilter.REQUEST_ID_HEADER)).isEqualTo("client-request_42");
        ILoggingEvent event = singleEvent();
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getFormattedMessage()).isEqualTo("http_request_completed");
        assertThat(event.getMDCPropertyMap()).containsEntry("requestId", "client-request_42");
        assertThat(keyValues(event))
                .containsEntry("event.action", "http_request_completed")
                .containsEntry("http.request.method", "GET")
                .containsEntry("url.path", "/api/households/{id}")
                .containsEntry("http.response.status_code", 204)
                .containsKey("event.duration_ms");
        assertThat(event.getThreadName()).isEqualTo(Thread.currentThread().getName());
        assertThat(event.getInstant()).isNotNull();
        assertStructuredEcsFields(event);
        assertThat(MDC.getCopyOfContextMap()).isNull();
    }

    @Test
    void shouldGenerateRequestIdWhenIncomingValueIsInvalid() throws Exception {
        var request = request("GET", "/api/households");
        request.addHeader(HttpRequestLoggingFilter.REQUEST_ID_HEADER, "contains spaces and is invalid");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {});

        String generated = response.getHeader(HttpRequestLoggingFilter.REQUEST_ID_HEADER);
        assertThat(generated).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(singleEvent().getMDCPropertyMap()).containsEntry("requestId", generated);
    }

    @Test
    void shouldClearMdcAndAvoidSensitiveRequestDataWhenRequestFails() {
        var request = request("POST", "/api/auth/login");
        request.setQueryString("email=secret@example.com");
        request.addHeader("Authorization", "Bearer secret-token");
        request.addHeader("Cookie", "ncasa_refresh=secret-refresh-token");
        var response = new MockHttpServletResponse();

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                filter.doFilter(request, response, (servletRequest, servletResponse) -> {
                    throw new IllegalStateException("boom");
                })).isInstanceOf(IllegalStateException.class);

        ILoggingEvent event = singleEvent();
        String logged = event.getFormattedMessage() + keyValues(event) + event.getMDCPropertyMap();
        assertThat(logged)
                .doesNotContain("secret@example.com")
                .doesNotContain("secret-token")
                .doesNotContain("secret-refresh-token");
        assertThat(MDC.getCopyOfContextMap()).isNull();
    }

    @Test
    void shouldAddAuthenticatedTechnicalUserIdWithoutLoggingEmail() throws Exception {
        var request = request("GET", "/api/households");
        request.setAttribute(HttpRequestLoggingFilter.AUTHENTICATED_USER_ID_ATTRIBUTE, 73L);

        filter.doFilter(request, new MockHttpServletResponse(), (servletRequest, servletResponse) ->
                assertThat(MDC.get(HttpRequestLoggingFilter.USER_ID_MDC_KEY)).isEqualTo("73"));

        ILoggingEvent event = singleEvent();
        assertThat(event.getMDCPropertyMap()).containsEntry("userId", "73");
        assertThat(event.getFormattedMessage() + keyValues(event) + event.getMDCPropertyMap())
                .doesNotContain("private@example.com");
    }

    @Test
    void shouldKeepConcurrentRequestContextsIsolated() throws Exception {
        try (var executor = Executors.newFixedThreadPool(4)) {
            var futures = java.util.stream.IntStream.range(0, 20)
                    .mapToObj(index -> executor.submit(() -> {
                        var request = request("GET", "/api/households/" + index);
                        request.addHeader(HttpRequestLoggingFilter.REQUEST_ID_HEADER, "request-" + index);
                        filter.doFilter(request, new MockHttpServletResponse(), (servletRequest, servletResponse) ->
                                assertThat(MDC.get(HttpRequestLoggingFilter.REQUEST_ID_MDC_KEY))
                                        .isEqualTo("request-" + index));
                        return MDC.getCopyOfContextMap();
                    })).toList();

            for (var future : futures) {
                assertThat(future.get()).isNull();
            }
        }

        assertThat(appender.events).hasSize(20);
        assertThat(appender.events).extracting(event -> event.getMDCPropertyMap().get("requestId"))
                .containsExactlyInAnyOrder(java.util.stream.IntStream.range(0, 20)
                        .mapToObj(index -> "request-" + index).toArray(String[]::new));
    }

    private MockHttpServletRequest request(String method, String path) {
        var request = new MockHttpServletRequest(method, path);
        request.setRequestURI(path);
        return request;
    }

    private ILoggingEvent singleEvent() {
        assertThat(appender.events).hasSize(1);
        return appender.events.getFirst();
    }

    private Map<String, Object> keyValues(ILoggingEvent event) {
        return event.getKeyValuePairs().stream()
                .collect(Collectors.toMap(pair -> pair.key, pair -> pair.value));
    }

    private void assertStructuredEcsFields(ILoggingEvent event) throws Exception {
        var context = new LoggerContext();
        context.putObject(Environment.class.getName(),
                new MockEnvironment().withProperty("spring.application.name", "ncasa"));
        var encoder = new StructuredLogEncoder();
        encoder.setContext(context);
        encoder.setFormat("ecs");
        encoder.start();
        try {
            String encoded = new String(encoder.encode(event), StandardCharsets.UTF_8);
            var json = new ObjectMapper().readTree(encoded);
            assertThat(Instant.parse(json.get("@timestamp").asString())).isNotNull();
            assertThat(json.at("/process/thread/name").asString()).isEqualTo(Thread.currentThread().getName());
            assertThat(json.get("requestId").asString()).isEqualTo("client-request_42");
            assertThat(json.at("/event/action").asString()).isEqualTo("http_request_completed");
        } finally {
            encoder.stop();
            context.stop();
        }
    }

    private static final class CapturingAppender extends AppenderBase<ILoggingEvent> {
        private final List<ILoggingEvent> events = new CopyOnWriteArrayList<>();

        @Override
        protected void append(ILoggingEvent event) {
            events.add(event);
        }
    }
}
