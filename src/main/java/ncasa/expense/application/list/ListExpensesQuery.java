package ncasa.expense.application.list;

import java.time.LocalDate;
import java.util.UUID;
import ncasa.expense.domain.ExpenseStatus;

public record ListExpensesQuery(Long actorAccountId, UUID householdId, LocalDate from, LocalDate to,
        ExpenseStatus status, int page, int size) {}
