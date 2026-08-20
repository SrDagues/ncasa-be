package ncasa.expense.application.create;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateExpenseCommand(Long actorAccountId, UUID householdId, String description,
        BigDecimal amount, String currency, LocalDate expenseDate, UUID payerMemberId,
        ExpenseSplitCommand split) {}
