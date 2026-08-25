package ncasa.expense.application.plan;
import java.math.BigDecimal;import java.time.LocalDate;import java.util.*;import ncasa.expense.domain.*;
public record CreateExpensePlanCommand(Long actorAccountId,UUID householdId,String description,BigDecimal amount,
        String currency,UUID payerMemberId,UUID categoryId,PlanSplitCommand split,ExpensePlanFrequency frequency,
        LocalDate startDate,String zoneId,String endCondition,LocalDate endDate,Integer totalOccurrences,int reminderDaysBefore){}
