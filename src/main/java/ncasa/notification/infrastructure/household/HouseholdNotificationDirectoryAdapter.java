package ncasa.notification.infrastructure.household;

import java.util.*;import java.util.stream.Collectors;import ncasa.household.application.get.*;import ncasa.household.domain.AccountId;import ncasa.notification.application.port.out.HouseholdNotificationDirectoryPort;import org.springframework.stereotype.Component;

@Component public class HouseholdNotificationDirectoryAdapter implements HouseholdNotificationDirectoryPort{
    private final GetHouseholdNotificationDirectoryUseCase directory;private final ListAccountHouseholdsUseCase households;
    public HouseholdNotificationDirectoryAdapter(GetHouseholdNotificationDirectoryUseCase d,ListAccountHouseholdsUseCase h){directory=d;households=h;}
    public Directory get(UUID householdId){var value=directory.execute(householdId);return new Directory(value.active(),value.members().stream().map(m->new Member(m.memberId(),m.accountId(),m.active(),m.administrator())).toList());}
    public Set<UUID> activeHouseholdIds(Long accountId){return households.execute(new AccountId(accountId)).stream().map(h->h.id()).collect(Collectors.toUnmodifiableSet());}
}
