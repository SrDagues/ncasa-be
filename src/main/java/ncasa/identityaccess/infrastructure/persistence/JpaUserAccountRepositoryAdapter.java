package ncasa.identityaccess.infrastructure.persistence;

import java.util.Optional;
import java.util.stream.Collectors;
import ncasa.identityaccess.application.EmailAlreadyRegisteredException;
import ncasa.identityaccess.application.port.out.UserAccountRepository;
import ncasa.identityaccess.domain.AccountStatus;
import ncasa.identityaccess.domain.Email;
import ncasa.identityaccess.domain.GlobalRole;
import ncasa.identityaccess.domain.PasswordHash;
import ncasa.identityaccess.domain.UserAccount;
import ncasa.identityaccess.domain.UserId;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserAccountRepositoryAdapter implements UserAccountRepository {
    private static final String LOCAL = "LOCAL";
    private final SpringDataUserAccountRepository users;
    private final SpringDataAuthIdentityRepository identities;

    public JpaUserAccountRepositoryAdapter(SpringDataUserAccountRepository users,
            SpringDataAuthIdentityRepository identities) {
        this.users = users;
        this.identities = identities;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return users.existsByEmailIgnoreCase(email.value());
    }

    @Override
    public Optional<UserAccount> findByEmail(Email email) {
        return identities.findByProviderAndProviderUserId(LOCAL, email.value()).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findById(UserId id) {
        return identities.findByProviderAndUser_Id(LOCAL, id.value()).map(this::toDomain);
    }

    @Override
    public UserAccount save(UserAccount account) {
        if (account.id() != null) throw new UnsupportedOperationException("Updating accounts is not implemented yet");
        try {
            var user = users.save(new JpaUserAccountEntity(account.email().value(), account.canAuthenticate(),
                    account.roles().stream().map(Enum::name).collect(Collectors.toSet()),
                    account.createdAt(), account.updatedAt()));
            identities.save(new JpaAuthIdentityEntity(user, LOCAL, account.email().value(),
                    account.passwordHash().value(), account.createdAt()));
            return account.withId(new UserId(user.id()));
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyRegisteredException();
        }
    }

    private UserAccount toDomain(JpaAuthIdentityEntity identity) {
        var user = identity.user();
        var roles = user.roles().stream().map(GlobalRole::valueOf).collect(Collectors.toSet());
        return UserAccount.rehydrate(new UserId(user.id()), Email.of(user.email()),
                new PasswordHash(identity.passwordHash()),
                user.enabled() ? AccountStatus.ACTIVE : AccountStatus.DISABLED,
                roles, user.createdAt(), user.updatedAt());
    }
}
