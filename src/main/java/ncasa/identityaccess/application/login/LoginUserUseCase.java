package ncasa.identityaccess.application.login;

import ncasa.identityaccess.application.AuthenticationResult;
import ncasa.identityaccess.application.InvalidCredentialsException;
import ncasa.identityaccess.application.EmailVerificationRequiredException;
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
        if (!passwordHasher.matches(rawPassword, account.passwordHash())
                || account.status() != ncasa.identityaccess.domain.AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }
        if (!account.isEmailVerified()) throw new EmailVerificationRequiredException();
        return sessions.issue(account).result();
    }
}
