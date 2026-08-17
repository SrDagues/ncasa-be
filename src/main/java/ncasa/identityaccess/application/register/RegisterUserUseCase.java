package ncasa.identityaccess.application.register;

import java.time.Clock;
import ncasa.identityaccess.application.AuthenticationResult;
import ncasa.identityaccess.application.EmailAlreadyRegisteredException;
import ncasa.identityaccess.application.port.out.PasswordHasher;
import ncasa.identityaccess.application.port.out.UserAccountRepository;
import ncasa.identityaccess.application.session.SessionIssuer;
import ncasa.identityaccess.domain.Email;
import ncasa.identityaccess.domain.UserAccount;

public final class RegisterUserUseCase {
    private final UserAccountRepository users;
    private final PasswordHasher passwordHasher;
    private final SessionIssuer sessions;
    private final Clock clock;

    public RegisterUserUseCase(UserAccountRepository users, PasswordHasher passwordHasher,
            SessionIssuer sessions, Clock clock) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.sessions = sessions;
        this.clock = clock;
    }

    public AuthenticationResult execute(String rawEmail, String rawPassword) {
        Email email = Email.of(rawEmail);
        if (users.existsByEmail(email)) throw new EmailAlreadyRegisteredException();
        UserAccount account = UserAccount.registered(email, passwordHasher.hash(rawPassword), clock.instant());
        account = users.save(account);
        return sessions.issue(account).result();
    }
}
