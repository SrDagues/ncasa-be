package ncasa.common.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class HttpRequestLoggingFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String USER_ID_MDC_KEY = "userId";
    public static final String AUTHENTICATED_USER_ID_ATTRIBUTE = HttpRequestLoggingFilter.class.getName() + ".userId";

    private static final Pattern VALID_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        String requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
        long startedAt = System.nanoTime();
        boolean failed = false;

        try {
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
            authenticatedUserId(request).ifPresent(userId -> MDC.put(USER_ID_MDC_KEY, userId));
            response.setHeader(REQUEST_ID_HEADER, requestId);
            chain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException | Error exception) {
            failed = true;
            throw exception;
        } finally {
            long durationMillis = (System.nanoTime() - startedAt) / 1_000_000;
            int status = failed && response.getStatus() < 400
                    ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                    : response.getStatus();
            LOGGER.atInfo()
                    .addKeyValue("event.action", "http_request_completed")
                    .addKeyValue("http.request.method", request.getMethod())
                    .addKeyValue("url.path", requestPath(request))
                    .addKeyValue("http.response.status_code", status)
                    .addKeyValue("event.duration_ms", durationMillis)
                    .log("http_request_completed");
            restoreMdc(previousContext);
        }
    }

    private String resolveRequestId(String candidate) {
        return candidate != null && VALID_REQUEST_ID.matcher(candidate).matches()
                ? candidate
                : UUID.randomUUID().toString();
    }

    private Optional<String> authenticatedUserId(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(AUTHENTICATED_USER_ID_ATTRIBUTE))
                .map(String::valueOf);
    }

    private String requestPath(HttpServletRequest request) {
        Object routePattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return routePattern instanceof String pattern ? pattern : request.getRequestURI();
    }

    private void restoreMdc(Map<String, String> previousContext) {
        if (previousContext == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(previousContext);
        }
    }
}
