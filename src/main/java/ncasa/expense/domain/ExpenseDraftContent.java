package ncasa.expense.domain;

import java.time.LocalDate;

public record ExpenseDraftContent(ExpenseDescription description, MemberRef payerMemberId, Money total,
        LocalDate expenseDate, ExpenseCategoryId categoryId, DraftSplit split) {}
