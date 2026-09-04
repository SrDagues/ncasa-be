package ncasa.expense.application.draft;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import ncasa.expense.application.create.ExpenseSplitCommand;

public record UpdateExpenseDraftCommand(Long actorAccountId,UUID householdId,UUID draftId,String description,
        BigDecimal amount,String currency,LocalDate expenseDate,UUID payerMemberId,UUID categoryId,
        ExpenseSplitCommand split,long version){}
