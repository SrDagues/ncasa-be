package ncasa.common.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import ncasa.identityaccess.application.EmailAlreadyRegisteredException;
import ncasa.identityaccess.application.InvalidCredentialsException;
import ncasa.identityaccess.application.InvalidRefreshTokenException;
import ncasa.identityaccess.domain.InvalidEmailException;
import ncasa.household.application.HouseholdNotFoundException;
import ncasa.household.application.InvitationNotFoundException;
import ncasa.household.domain.HouseholdAccessDeniedException;
import ncasa.household.domain.HouseholdRuleViolationException;
import ncasa.household.domain.InvitationExpiredException;
import ncasa.household.domain.InvitationStateException;
import ncasa.expense.application.ExpenseAccessDeniedException;
import ncasa.expense.application.ExpenseNotFoundException;
import ncasa.expense.application.SettlementNotFoundException;
import ncasa.expense.application.SettlementConflictException;
import ncasa.expense.domain.ExpenseRuleViolationException;
import ncasa.expense.domain.ExpenseStateException;
import ncasa.expense.domain.SettlementStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingRequestHeaderException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ApiError> conflict(EmailAlreadyRegisteredException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
    ResponseEntity<ApiError> unauthorized(RuntimeException ex) {
        return response(HttpStatus.UNAUTHORIZED, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidEmailException.class)
    ResponseEntity<ApiError> invalidEmail(InvalidEmailException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of("email", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "Validation failed", fields);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class})
    ResponseEntity<ApiError> malformedInput(Exception ex) {
        return response(HttpStatus.BAD_REQUEST, "Malformed request", Map.of());
    }

    @ExceptionHandler({HouseholdAccessDeniedException.class, ExpenseAccessDeniedException.class})
    ResponseEntity<ApiError> forbidden(RuntimeException ex) {
        return response(HttpStatus.FORBIDDEN, ex.getMessage(), Map.of());
    }

    @ExceptionHandler({HouseholdNotFoundException.class, InvitationNotFoundException.class,
            ExpenseNotFoundException.class, SettlementNotFoundException.class})
    ResponseEntity<ApiError> notFound(RuntimeException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(InvitationExpiredException.class)
    ResponseEntity<ApiError> expired(InvitationExpiredException ex) {
        return response(HttpStatus.GONE, ex.getMessage(), Map.of());
    }

    @ExceptionHandler({HouseholdRuleViolationException.class, InvitationStateException.class})
    ResponseEntity<ApiError> householdConflict(RuntimeException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    @ExceptionHandler({ExpenseStateException.class, SettlementStateException.class, SettlementConflictException.class})
    ResponseEntity<ApiError> expenseConflict(RuntimeException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(ExpenseRuleViolationException.class)
    ResponseEntity<ApiError> invalidExpense(ExpenseRuleViolationException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
    ResponseEntity<ApiError> persistenceConflict(RuntimeException ex) {
        return response(HttpStatus.CONFLICT, "Resource changed concurrently", Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> invalidHouseholdInput(IllegalArgumentException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex) {
        LOGGER.atError()
                .addKeyValue("event.action", "unhandled_request_exception")
                .setCause(ex)
                .log("unhandled_request_exception");
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message, Map<String, String> fields) {
        return ResponseEntity.status(status).body(
                new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, fields,
                        MDC.get("requestId")));
    }
}
