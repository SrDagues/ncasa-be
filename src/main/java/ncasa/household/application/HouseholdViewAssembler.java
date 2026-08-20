package ncasa.household.application;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ncasa.household.application.port.out.AccountDirectoryPort;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.Household;

public final class HouseholdViewAssembler {
    private final AccountDirectoryPort accounts;

    public HouseholdViewAssembler(AccountDirectoryPort accounts) {
        this.accounts = accounts;
    }

    public HouseholdView assemble(Household household) {
        Set<AccountId> ids = household.members().stream()
                .map(member -> member.accountId()).collect(Collectors.toSet());
        Map<AccountId, String> emails = accounts.findEmails(ids);
        return HouseholdView.from(household, emails);
    }
}
