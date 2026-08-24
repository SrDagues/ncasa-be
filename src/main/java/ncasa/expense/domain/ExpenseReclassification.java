package ncasa.expense.domain;

public record ExpenseReclassification(ExpenseCategoryId previousCategoryId, ExpenseCategoryId newCategoryId) {}
