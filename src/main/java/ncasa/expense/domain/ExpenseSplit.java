package ncasa.expense.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class ExpenseSplit {
    private final ExpenseSplitType type;
    private final Money total;
    private final List<ExpenseAllocation> allocations;

    private ExpenseSplit(ExpenseSplitType type, Money total, Collection<ExpenseAllocation> allocations) {
        this.type = Objects.requireNonNull(type);
        this.total = Objects.requireNonNull(total, "Expense total is required");
        if (allocations == null || allocations.isEmpty()) {
            throw new ExpenseRuleViolationException("At least one allocation is required");
        }
        this.allocations = List.copyOf(allocations);
        var members = new HashSet<MemberRef>();
        Money allocated = new Money(BigDecimal.ZERO, total.currency());
        for (ExpenseAllocation allocation : this.allocations) {
            if (!members.add(allocation.memberId())) {
                throw new ExpenseRuleViolationException("A member cannot appear twice in an expense split");
            }
            allocation.amount().requireSameCurrency(total);
            allocated = allocated.add(allocation.amount());
        }
        if (allocated.amount().compareTo(total.amount()) != 0) {
            throw new ExpenseRuleViolationException("Allocations must add up to the expense total");
        }
    }

    public static ExpenseSplit exact(Money total, Collection<ExpenseAllocation> allocations) {
        return new ExpenseSplit(ExpenseSplitType.EXACT, total, allocations);
    }

    public static ExpenseSplit rehydrate(ExpenseSplitType type, Money total,
            Collection<ExpenseAllocation> allocations) {
        return new ExpenseSplit(type, total, allocations);
    }

    public static ExpenseSplit equal(Money total, Collection<MemberRef> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new ExpenseRuleViolationException("At least one participant is required");
        }
        var sorted = participants.stream().sorted(Comparator.naturalOrder()).toList();
        if (new HashSet<>(sorted).size() != sorted.size()) {
            throw new ExpenseRuleViolationException("A member cannot appear twice in an expense split");
        }
        int scale = total.fractionDigits();
        BigInteger totalMinorUnits = total.amount().movePointRight(scale).toBigIntegerExact();
        BigInteger[] division = totalMinorUnits.divideAndRemainder(BigInteger.valueOf(sorted.size()));
        int remainder = division[1].intValueExact();
        var allocations = new ArrayList<ExpenseAllocation>(sorted.size());
        for (int index = 0; index < sorted.size(); index++) {
            BigInteger units = division[0].add(index < remainder ? BigInteger.ONE : BigInteger.ZERO);
            Money amount = new Money(new BigDecimal(units, scale), total.currency());
            allocations.add(new ExpenseAllocation(sorted.get(index), amount));
        }
        return new ExpenseSplit(ExpenseSplitType.EQUAL, total, allocations);
    }

    public ExpenseSplitType type() { return type; }
    public Money total() { return total; }
    public List<ExpenseAllocation> allocations() { return allocations; }
}
