package ncasa.expense.application.port.out;

import java.util.List;
import ncasa.expense.domain.Expense;

public record ExpensePageSlice(List<Expense> items, long totalElements) {
    public ExpensePageSlice { items = List.copyOf(items); }
}
