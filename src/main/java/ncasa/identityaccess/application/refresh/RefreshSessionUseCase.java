package ncasa.identityaccess.application.refresh;

import ncasa.auth.dto.TokenResponse;
import ncasa.identityaccess.application.port.out.AuthenticationOperations;
import org.springframework.stereotype.Service;

@Service
public class RefreshSessionUseCase {
    private final AuthenticationOperations authentication;

    public RefreshSessionUseCase(AuthenticationOperations authentication) {
        this.authentication = authentication;
    }

    public TokenResponse execute(String refreshToken) {
        return authentication.refresh(refreshToken);
    }
}
