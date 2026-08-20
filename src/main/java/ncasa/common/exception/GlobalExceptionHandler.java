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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
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

    @ExceptionHandler(HouseholdAccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(HouseholdAccessDeniedException ex) {
        return response(HttpStatus.FORBIDDEN, ex.getMessage(), Map.of());
    }

    @ExceptionHandler({HouseholdNotFoundException.class, InvitationNotFoundException.class})
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

    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
    ResponseEntity<ApiError> persistenceConflict(RuntimeException ex) {
        return response(HttpStatus.CONFLICT, "Household changed concurrently", Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> invalidHouseholdInput(IllegalArgumentException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message, Map<String, String> fields) {
        return ResponseEntity.status(status).body(
                new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, fields));
    }
}
