package ncasa.identityaccess.application.port.out;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import ncasa.identityaccess.domain.Email;
import ncasa.identityaccess.domain.UserAccount;
import ncasa.identityaccess.domain.UserId;

public interface UserAccountRepository {
    boolean existsByEmail(Email email);
    Optional<UserAccount> findByEmail(Email email);
    Optional<UserAccount> findById(UserId id);
    Map<UserId, Email> findEmailsByIds(Set<UserId> ids);
    UserAccount save(UserAccount account);
}
