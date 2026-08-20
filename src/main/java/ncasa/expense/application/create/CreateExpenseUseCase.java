package ncasa.expense.application.create;

import java.time.Clock;
import java.util.UUID;
import ncasa.expense.application.ExpenseView;
import ncasa.expense.application.port.out.ExpenseRepository;
import ncasa.expense.application.port.out.HouseholdExpenseAccessPort;
import ncasa.expense.domain.*;

public final class CreateExpenseUseCase {
    private final ExpenseRepository expenses;
    private final HouseholdExpenseAccessPort householdAccess;
    private final Clock clock;

    public CreateExpenseUseCase(ExpenseRepository expenses, HouseholdExpenseAccessPort householdAccess, Clock clock) {
        this.expenses = expenses;
        this.householdAccess = householdAccess;
        this.clock = clock;
    }

    public ExpenseView execute(CreateExpenseCommand command) {
        var householdId = new HouseholdRef(command.householdId());
        var context = householdAccess.getContext(householdId, command.actorAccountId());
        var payer = new MemberRef(command.payerMemberId());
        context.requireActive(payer);
        var total = new Money(command.amount(), command.currency());
        var split = createSplit(command.split(), total, context);
        var expense = Expense.confirmedManual(new ExpenseId(UUID.randomUUID()), householdId,
                context.actorMemberId(), payer, total, new ExpenseDescription(command.description()),
                command.expenseDate(), split, clock.instant());
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
        throw new ExpenseRuleViolationException("Expense split is required");
    }
}
