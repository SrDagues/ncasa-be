package ncasa.expense.application.list;

import java.time.LocalDate;
import java.util.UUID;
import ncasa.expense.domain.*;

public record ListExpensesQuery(Long actorAccountId, UUID householdId, LocalDate from, LocalDate to,
        ExpenseStatus status, UUID payerMemberId, UUID participantMemberId, UUID categoryId,
        boolean uncategorized, ExpenseSplitType splitType, int page, int size) {
    public ListExpensesQuery(Long actorAccountId, UUID householdId, LocalDate from, LocalDate to,
            ExpenseStatus status, UUID payerMemberId, UUID participantMemberId, int page, int size) {
        this(actorAccountId, householdId, from, to, status, payerMemberId, participantMemberId,
                null, false, null, page, size);
    }
    public ListExpensesQuery(Long actorAccountId, UUID householdId, LocalDate from, LocalDate to,
            ExpenseStatus status, int page, int size) {
        this(actorAccountId, householdId, from, to, status, null, null, null, false, null, page, size);
    }
}
