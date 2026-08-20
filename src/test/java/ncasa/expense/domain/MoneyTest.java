package ncasa.expense.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {
    @Test
    void shouldNormalizeAmountToCurrencyFractionDigits() {
        Money money = new Money(new BigDecimal("10"), "eur");
        assertThat(money.amount()).isEqualByComparingTo("10.00");
        assertThat(money.currency()).isEqualTo("EUR");
    }

    @Test
    void shouldRejectTooManyFractionDigits() {
        assertThatThrownBy(() -> Money.of("10.001", "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectAddingDifferentCurrencies() {
        assertThatThrownBy(() -> Money.of("10", "EUR").add(Money.of("10", "USD")))
                .isInstanceOf(ExpenseRuleViolationException.class);
    }

    @Test
    void shouldRejectAmountOutsidePersistencePrecision() {
        assertThatThrownBy(() -> Money.of("1000000000000000.00", "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
