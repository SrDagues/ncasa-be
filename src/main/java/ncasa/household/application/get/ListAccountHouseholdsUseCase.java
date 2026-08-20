package ncasa.household.application.get;

import java.util.Comparator;
import java.util.List;
import ncasa.household.application.HouseholdSummaryView;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.AccountId;

public final class ListAccountHouseholdsUseCase {
    private final HouseholdRepository households;

    public ListAccountHouseholdsUseCase(HouseholdRepository households) {
        this.households = households;
    }

    public List<HouseholdSummaryView> execute(AccountId accountId) {
        return households.findActiveByMemberAccountId(accountId).stream()
                .sorted(Comparator.comparing(household -> household.createdAt()))
                .map(household -> HouseholdSummaryView.from(household, household.activeMemberFor(accountId)))
                .toList();
    }
}
