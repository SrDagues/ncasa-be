package ncasa.expense.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static ExpenseSplit percentage(Money total, Collection<PercentageAllocation> percentages) {
        if (percentages == null || percentages.isEmpty()) {
            throw new ExpenseRuleViolationException("At least one percentage allocation is required");
        }
        var members = new HashSet<MemberRef>();
        int percentageTotal = 0;
        for (PercentageAllocation allocation : percentages) {
            if (!members.add(allocation.memberId())) {
                throw new ExpenseRuleViolationException("A member cannot appear twice in an expense split");
            }
            percentageTotal = Math.addExact(percentageTotal, allocation.percentage().basisPoints());
        }
        if (percentageTotal != Percentage.TOTAL_BASIS_POINTS) {
            throw new ExpenseRuleViolationException("Percentages must add up to 100.00");
        }

        int scale = total.fractionDigits();
        BigInteger totalUnits = total.amount().movePointRight(scale).toBigIntegerExact();
        record Share(PercentageAllocation allocation, BigInteger units, BigInteger remainder) {}
        var shares = percentages.stream().map(allocation -> {
            BigInteger weighted = totalUnits.multiply(BigInteger.valueOf(allocation.percentage().basisPoints()));
            BigInteger[] division = weighted.divideAndRemainder(BigInteger.valueOf(Percentage.TOTAL_BASIS_POINTS));
            return new Share(allocation, division[0], division[1]);
        }).toList();
        BigInteger assigned = shares.stream().map(Share::units).reduce(BigInteger.ZERO, BigInteger::add);
        int remaining = totalUnits.subtract(assigned).intValueExact();
        var priority = shares.stream().sorted(Comparator.comparing(Share::remainder).reversed()
                .thenComparing(share -> share.allocation().memberId())).toList();
        Map<MemberRef, Integer> bonus = priority.stream().limit(remaining)
                .map(share -> share.allocation().memberId())
                .collect(Collectors.toMap(Function.identity(), ignored -> 1));
        var allocations = shares.stream()
                .sorted(Comparator.comparing(share -> share.allocation().memberId()))
                .map(share -> new ExpenseAllocation(share.allocation().memberId(),
                        new Money(new BigDecimal(share.units().add(BigInteger.valueOf(
                                bonus.getOrDefault(share.allocation().memberId(), 0))), scale), total.currency())))
                .toList();
        return new ExpenseSplit(ExpenseSplitType.PERCENTAGE, total, allocations);
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
