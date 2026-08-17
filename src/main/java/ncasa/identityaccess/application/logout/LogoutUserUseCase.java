package ncasa.identityaccess.application.logout;

import ncasa.identityaccess.application.port.out.AuthenticationOperations;
import org.springframework.stereotype.Service;

@Service
public class LogoutUserUseCase {
    private final AuthenticationOperations authentication;

    public LogoutUserUseCase(AuthenticationOperations authentication) {
        this.authentication = authentication;
    }

    public void execute(String refreshToken, Long userId) {
        authentication.logout(refreshToken, userId);
    }
}
