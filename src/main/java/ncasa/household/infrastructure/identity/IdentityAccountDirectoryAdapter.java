package ncasa.household.infrastructure.identity;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ncasa.household.application.port.out.AccountDirectoryPort;
import ncasa.household.domain.AccountId;
import ncasa.identityaccess.application.port.out.UserAccountRepository;
import ncasa.identityaccess.domain.UserId;
import org.springframework.stereotype.Component;

@Component
public class IdentityAccountDirectoryAdapter implements AccountDirectoryPort {
    private final UserAccountRepository accounts;

    public IdentityAccountDirectoryAdapter(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public Map<AccountId, String> findEmails(Set<AccountId> accountIds) {
        Set<UserId> userIds = accountIds.stream().map(id -> new UserId(id.value())).collect(Collectors.toSet());
        return accounts.findEmailsByIds(userIds).entrySet().stream().collect(Collectors.toMap(
                entry -> new AccountId(entry.getKey().value()), entry -> entry.getValue().value()));
    }
}
