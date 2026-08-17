package ncasa.identityaccess.application.port.out;

import ncasa.auth.dto.LoginRequest;
import ncasa.auth.dto.RegisterRequest;
import ncasa.auth.dto.TokenResponse;

/**
 * Transitional outbound port used while the existing authentication implementation
 * is migrated behind the Identity & Access bounded context.
 */
public interface AuthenticationOperations {
    TokenResponse register(RegisterRequest request);
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(String refreshToken);
    void logout(String refreshToken, Long userId);
}
