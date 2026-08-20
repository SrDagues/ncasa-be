package ncasa.expense.infrastructure.web;

import java.util.List;
import ncasa.expense.application.ExpensePage;

public record ExpensePageResponse(List<ExpenseResponse> items, int page, int size,
        long totalElements, int totalPages) {
    static ExpensePageResponse from(ExpensePage page) {
        return new ExpensePageResponse(page.items().stream().map(ExpenseResponse::from).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
