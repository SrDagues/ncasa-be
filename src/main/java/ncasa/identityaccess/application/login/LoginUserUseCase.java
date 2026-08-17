package ncasa.identityaccess.application.login;

import ncasa.identityaccess.application.AuthenticationResult;
import ncasa.identityaccess.application.InvalidCredentialsException;
import ncasa.identityaccess.application.port.out.PasswordHasher;
import ncasa.identityaccess.application.port.out.UserAccountRepository;
import ncasa.identityaccess.application.session.SessionIssuer;
import ncasa.identityaccess.domain.Email;

public final class LoginUserUseCase {
    private final UserAccountRepository users;
    private final PasswordHasher passwordHasher;
    private final SessionIssuer sessions;

    public LoginUserUseCase(UserAccountRepository users, PasswordHasher passwordHasher, SessionIssuer sessions) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.sessions = sessions;
    }

    public AuthenticationResult execute(String rawEmail, String rawPassword) {
        var account = users.findByEmail(Email.of(rawEmail)).orElseThrow(InvalidCredentialsException::new);
        if (!account.canAuthenticate() || !passwordHasher.matches(rawPassword, account.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        return sessions.issue(account).result();
    }
}
