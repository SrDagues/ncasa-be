package ncasa.expense.application.create;

import java.time.Clock;
import java.util.UUID;
import ncasa.expense.application.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;

public final class CreateExpenseUseCase {
    private final ExpenseRepository expenses;
    private final HouseholdExpenseAccessPort householdAccess;
    private final ExpenseCategoryRepository categories;
    private final Clock clock;

    public CreateExpenseUseCase(ExpenseRepository expenses, HouseholdExpenseAccessPort householdAccess, Clock clock) {
        this(expenses, householdAccess, null, clock);
    }

    public CreateExpenseUseCase(ExpenseRepository expenses, HouseholdExpenseAccessPort householdAccess,
            ExpenseCategoryRepository categories, Clock clock) {
        this.expenses = expenses; this.householdAccess = householdAccess;
        this.categories = categories; this.clock = clock;
    }

    public ExpenseView execute(CreateExpenseCommand command) {
        var householdId = new HouseholdRef(command.householdId());
        var context = householdAccess.getContext(householdId, command.actorAccountId());
        var payer = new MemberRef(command.payerMemberId());
        context.requireActive(payer);
        var total = new Money(command.amount(), command.currency());
        var split = createSplit(command.split(), total, context);
        var categoryId = resolveCategory(command.categoryId(), householdId);
        var expense = Expense.confirmedManual(new ExpenseId(UUID.randomUUID()), householdId,
                context.actorMemberId(), payer, total, new ExpenseDescription(command.description()),
                command.expenseDate(), split, categoryId, clock.instant());
        return ExpenseView.from(expenses.save(expense));
    }

    private ExpenseSplit createSplit(ExpenseSplitCommand command, Money total,
            ncasa.expense.application.ExpenseHouseholdContext context) {
        if (command instanceof EqualSplitCommand equal) {
            var members = equal.memberIds().stream().map(MemberRef::new).toList();
            members.forEach(context::requireActive);
            return ExpenseSplit.equal(total, members);
        }
        if (command instanceof ExactSplitCommand exact) {
            var allocations = exact.allocations().stream().map(raw -> {
                var member = new MemberRef(raw.memberId());
                context.requireActive(member);
                return new ExpenseAllocation(member, new Money(raw.amount(), total.currency()));
            }).toList();
            return ExpenseSplit.exact(total, allocations);
        }
        if (command instanceof PercentageSplitCommand percentage) {
            var allocations = percentage.allocations().stream().map(raw -> {
                var member = new MemberRef(raw.memberId());
                context.requireActive(member);
                return new PercentageAllocation(member, Percentage.of(raw.percentage()));
            }).toList();
            return ExpenseSplit.percentage(total, allocations);
        }
        throw new ExpenseRuleViolationException("Expense split is required");
    }

    private ExpenseCategoryId resolveCategory(UUID rawCategoryId, HouseholdRef householdId) {
        if (rawCategoryId == null) return null;
        if (categories == null) throw new ExpenseRuleViolationException("Expense categories are not configured");
        var category = categories.findByIdAndHousehold(new ExpenseCategoryId(rawCategoryId), householdId)
                .orElseThrow(() -> new CategoryNotFoundException("Expense category not found"));
        category.requireActive();
        return category.id();
    }
}
