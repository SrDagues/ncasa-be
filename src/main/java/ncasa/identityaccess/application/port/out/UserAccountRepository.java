package ncasa.identityaccess.application.port.out;

import java.util.Optional;
import ncasa.identityaccess.domain.Email;
import ncasa.identityaccess.domain.UserAccount;
import ncasa.identityaccess.domain.UserId;

public interface UserAccountRepository {
    boolean existsByEmail(Email email);
    Optional<UserAccount> findByEmail(Email email);
    Optional<UserAccount> findById(UserId id);
    UserAccount save(UserAccount account);
}
