package ncasa.expense.application.list;

import ncasa.expense.application.ExpensePage;
import ncasa.expense.application.ExpenseView;
import ncasa.expense.application.port.out.ExpenseRepository;
import ncasa.expense.application.port.out.HouseholdExpenseAccessPort;
import ncasa.expense.domain.HouseholdRef;

public final class ListExpensesUseCase {
    public static final int MAX_PAGE_SIZE = 100;
    private final ExpenseRepository expenses;
    private final HouseholdExpenseAccessPort householdAccess;
    public ListExpensesUseCase(ExpenseRepository expenses, HouseholdExpenseAccessPort householdAccess) {
        this.expenses = expenses; this.householdAccess = householdAccess;
    }
    public ExpensePage execute(ListExpensesQuery query) {
        if (query.page() < 0) throw new IllegalArgumentException("Page cannot be negative");
        if (query.size() < 1 || query.size() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new IllegalArgumentException("From date must not be after to date");
        }
        var household = new HouseholdRef(query.householdId());
        var context = householdAccess.getContext(household, query.actorAccountId());
        var payer = query.payerMemberId() == null ? null : new ncasa.expense.domain.MemberRef(query.payerMemberId());
        var participant = query.participantMemberId() == null ? null : new ncasa.expense.domain.MemberRef(query.participantMemberId());
        if (payer != null) context.requireMember(payer);
        if (participant != null) context.requireMember(participant);
        var result = expenses.findPage(household, query.from(), query.to(), query.status(), payer, participant, query.page(), query.size());
        int totalPages = result.totalElements() == 0 ? 0
                : (int) Math.ceil((double) result.totalElements() / query.size());
        return new ExpensePage(result.items().stream().map(ExpenseView::from).toList(), query.page(), query.size(),
                result.totalElements(), totalPages);
    }
}
