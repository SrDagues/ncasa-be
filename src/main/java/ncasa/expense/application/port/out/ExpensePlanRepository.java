package ncasa.expense.application.port.out;
import java.time.*;import java.util.*;import ncasa.expense.domain.*;
public interface ExpensePlanRepository {
    ExpensePlan save(ExpensePlan plan);
    Optional<ExpensePlan> findByIdAndHousehold(ExpensePlanId id,HouseholdRef householdId);
    ExpensePlanPageSlice findPage(HouseholdRef householdId,ExpensePlanStatus status,MemberRef payer,MemberRef participant,
            ExpensePlanFrequency frequency,LocalDate nextFrom,LocalDate nextTo,int page,int size);
    List<ExpensePlan> findActiveForForecast(HouseholdRef householdId);
    List<ExpensePlanId> findDueIds(Instant dueAt,int limit);
    Optional<ExpensePlan> findByIdForUpdate(ExpensePlanId id);
    List<ExpensePlanId> findReminderDueIds(Instant dueAt,int limit);
}
