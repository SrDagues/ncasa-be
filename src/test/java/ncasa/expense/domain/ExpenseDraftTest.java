package ncasa.expense.domain;

import static org.assertj.core.api.Assertions.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExpenseDraftTest {
    @Test void shouldAllowPartialContentAndProtectLifecycle() {
        var now = Instant.parse("2026-08-24T10:00:00Z");
        var draft = ExpenseDraft.create(new ExpenseDraftId(UUID.randomUUID()),
                new HouseholdRef(UUID.randomUUID()), new MemberRef(UUID.randomUUID()), now);
        draft.update(new ExpenseDraftContent(new ExpenseDescription("Pending"), null, null,
                null, null, null), now.plusSeconds(1));
        var expenseId = new ExpenseId(UUID.randomUUID());
        draft.confirm(expenseId, now.plusSeconds(2));
        assertThat(draft.status()).isEqualTo(ExpenseDraftStatus.CONFIRMED);
        assertThat(draft.confirmedExpenseId()).isEqualTo(expenseId);
        assertThatThrownBy(() -> draft.discard(now.plusSeconds(3))).isInstanceOf(ExpenseStateException.class);
    }

    @Test void shouldDiscardAnOpenDraft() {
        var now = Instant.parse("2026-08-24T10:00:00Z");
        var draft = ExpenseDraft.create(new ExpenseDraftId(UUID.randomUUID()),
                new HouseholdRef(UUID.randomUUID()), new MemberRef(UUID.randomUUID()), now);
        draft.discard(now.plusSeconds(1));
        assertThat(draft.status()).isEqualTo(ExpenseDraftStatus.DISCARDED);
    }
}
