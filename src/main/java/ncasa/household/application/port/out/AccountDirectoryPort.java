package ncasa.household.application.port.out;

import java.util.Map;
import java.util.Set;
import ncasa.household.domain.AccountId;

public interface AccountDirectoryPort {
    Map<AccountId, String> findEmails(Set<AccountId> accountIds);
}
