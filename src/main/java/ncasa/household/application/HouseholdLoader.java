package ncasa.household.application;

import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.Household;
import ncasa.household.domain.HouseholdId;

public final class HouseholdLoader {
    private HouseholdLoader() {}
    public static Household load(HouseholdRepository repository, HouseholdId id) {
        return repository.findById(id).orElseThrow(HouseholdNotFoundException::new);
    }
}
