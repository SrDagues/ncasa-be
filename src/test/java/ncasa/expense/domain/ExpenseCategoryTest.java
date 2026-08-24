package ncasa.expense.domain;

import static org.assertj.core.api.Assertions.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExpenseCategoryTest {
    @Test void shouldRenameAndArchiveActiveCategory() {
        var now = Instant.parse("2026-08-24T10:00:00Z");
        var category = ExpenseCategory.create(new ExpenseCategoryId(UUID.randomUUID()),
                new HouseholdRef(UUID.randomUUID()), new MemberRef(UUID.randomUUID()),
                new CategoryName(" Food "), now);
        category.rename(new CategoryName("Groceries"), now.plusSeconds(1));
        category.archive(now.plusSeconds(2));
        assertThat(category.name().value()).isEqualTo("Groceries");
        assertThat(category.status()).isEqualTo(ExpenseCategoryStatus.ARCHIVED);
        assertThatThrownBy(() -> category.rename(new CategoryName("Other"), now.plusSeconds(3)))
                .isInstanceOf(ExpenseStateException.class);
    }

    @Test void shouldRejectBlankOrLongNames() {
        assertThatThrownBy(() -> new CategoryName(" ")).isInstanceOf(ExpenseRuleViolationException.class);
        assertThatThrownBy(() -> new CategoryName("x".repeat(81))).isInstanceOf(ExpenseRuleViolationException.class);
    }
}
