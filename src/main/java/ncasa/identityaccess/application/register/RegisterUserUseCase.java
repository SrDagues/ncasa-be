package ncasa.identityaccess.application.register;

import ncasa.auth.dto.RegisterRequest;
import ncasa.auth.dto.TokenResponse;
import ncasa.identityaccess.application.port.out.AuthenticationOperations;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserUseCase {
    private final AuthenticationOperations authentication;

    public RegisterUserUseCase(AuthenticationOperations authentication) {
        this.authentication = authentication;
    }

    public TokenResponse execute(RegisterRequest request) {
        return authentication.register(request);
    }
}
