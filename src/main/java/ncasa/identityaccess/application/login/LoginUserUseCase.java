package ncasa.identityaccess.application.login;

import ncasa.auth.dto.LoginRequest;
import ncasa.auth.dto.TokenResponse;
import ncasa.identityaccess.application.port.out.AuthenticationOperations;
import org.springframework.stereotype.Service;

@Service
public class LoginUserUseCase {
    private final AuthenticationOperations authentication;

    public LoginUserUseCase(AuthenticationOperations authentication) {
        this.authentication = authentication;
    }

    public TokenResponse execute(LoginRequest request) {
        return authentication.login(request);
    }
}
