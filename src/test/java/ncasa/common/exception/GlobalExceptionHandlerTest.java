package ncasa.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void captureLogs() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        MDC.put("requestId", "request-500");
    }

    @AfterEach
    void cleanUp() {
        logger.detachAppender(appender);
        MDC.clear();
    }

    @Test
    void shouldLogUnexpectedExceptionAndReturnSafeCorrelatedError() {
        var response = handler.unexpected(new IllegalStateException("boom"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isNotNull();
        assertThat(response.getBody().requestId()).isEqualTo("request-500");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(appender.list.getFirst().getFormattedMessage()).isEqualTo("unhandled_request_exception");
        assertThat(appender.list.getFirst().getThrowableProxy()).isNotNull();
    }
}
