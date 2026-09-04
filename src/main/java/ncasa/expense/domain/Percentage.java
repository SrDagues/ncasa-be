package ncasa.expense.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Percentage(int basisPoints) implements Comparable<Percentage> {
    public static final int TOTAL_BASIS_POINTS = 10_000;

    public Percentage {
        if (basisPoints <= 0 || basisPoints > TOTAL_BASIS_POINTS) {
            throw new ExpenseRuleViolationException("Percentage must be greater than zero and at most 100.00");
        }
    }

    public static Percentage of(BigDecimal value) {
        if (value == null) throw new ExpenseRuleViolationException("Percentage is required");
        try {
            return new Percentage(value.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).intValueExact());
        } catch (ArithmeticException exception) {
            throw new ExpenseRuleViolationException("Percentage supports at most two decimal places");
        }
    }

    public BigDecimal value() {
        return BigDecimal.valueOf(basisPoints, 2);
    }

    @Override
    public int compareTo(Percentage other) {
        return Integer.compare(basisPoints, other.basisPoints);
    }
}
