package ncasa.expense.application;

import java.util.List;

public record ExpensePage(List<ExpenseView> items, int page, int size, long totalElements, int totalPages) {
    public ExpensePage {
        items = List.copyOf(items);
    }
}
