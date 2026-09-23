package ncasa.identityaccess.application.register;

import java.time.Clock;
import ncasa.identityaccess.application.EmailAlreadyRegisteredException;
import ncasa.identityaccess.application.RegistrationResult;
import ncasa.identityaccess.application.port.out.PasswordHasher;
import ncasa.identityaccess.application.port.out.UserAccountRepository;
import ncasa.identityaccess.application.verification.IssueEmailVerification;
import ncasa.identityaccess.domain.Email;
import ncasa.identityaccess.domain.UserAccount;

public final class RegisterUserUseCase {
    private final UserAccountRepository users;
    private final PasswordHasher passwordHasher;
    private final IssueEmailVerification emailVerification;
    private final Clock clock;

    public RegisterUserUseCase(UserAccountRepository users, PasswordHasher passwordHasher,
            IssueEmailVerification emailVerification, Clock clock) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.emailVerification = emailVerification;
        this.clock = clock;
    }

    public RegistrationResult execute(String rawEmail, String rawPassword) {
        Email email = Email.of(rawEmail);
        if (users.existsByEmail(email)) throw new EmailAlreadyRegisteredException();
        UserAccount account = UserAccount.registered(email, passwordHasher.hash(rawPassword), clock.instant());
        account = users.save(account);
        emailVerification.issue(account.id(), account.email(), clock.instant());
        return RegistrationResult.pendingEmailVerification();
    }
}
