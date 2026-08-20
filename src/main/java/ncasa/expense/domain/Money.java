package ncasa.expense.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {
    public Money {
        Objects.requireNonNull(amount, "Amount is required");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency is required");
        currency = currency.trim().toUpperCase(Locale.ROOT);
        Currency resolved;
        try {
            resolved = Currency.getInstance(currency);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown currency: " + currency);
        }
        int fractionDigits = resolved.getDefaultFractionDigits();
        if (fractionDigits < 0 || fractionDigits > 4) {
            throw new IllegalArgumentException("Unsupported currency fraction digits: " + currency);
        }
        BigDecimal stripped = amount.stripTrailingZeros();
        int effectiveScale = Math.max(stripped.scale(), 0);
        if (effectiveScale > fractionDigits) {
            throw new IllegalArgumentException("Amount has too many fraction digits for " + currency);
        }
        amount = amount.setScale(fractionDigits);
        if (amount.precision() - amount.scale() > 15) {
            throw new IllegalArgumentException("Amount exceeds supported precision");
        }
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "Money is required");
        if (!currency.equals(other.currency)) {
            throw new ExpenseRuleViolationException("Money currencies must match");
        }
    }

    public int fractionDigits() {
        return amount.scale();
    }
}
