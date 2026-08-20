package ncasa.household.application.port.out;

import java.util.List;
import java.util.Optional;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.Household;
import ncasa.household.domain.HouseholdId;

public interface HouseholdRepository {
    Optional<Household> findById(HouseholdId id);
    List<Household> findActiveByMemberAccountId(AccountId accountId);
    Household save(Household household);
}
