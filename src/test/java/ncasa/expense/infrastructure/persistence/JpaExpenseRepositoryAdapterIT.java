package ncasa.expense.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import ncasa.expense.application.port.out.ExpenseRepository;
import ncasa.expense.domain.*;
import ncasa.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class JpaExpenseRepositoryAdapterIT extends PostgresIntegrationTest {
    @Autowired ExpenseRepository expenses;
    @Autowired JdbcTemplate jdbc;
    private UUID householdId;
    private UUID ownerMemberId;
    private UUID otherMemberId;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM expense_allocations");
        jdbc.update("DELETE FROM expenses");
        jdbc.update("DELETE FROM household_invitations");
        jdbc.update("DELETE FROM household_members");
        jdbc.update("DELETE FROM households");
        jdbc.update("DELETE FROM auth_identities");
        jdbc.update("DELETE FROM user_roles");
        jdbc.update("DELETE FROM refresh_tokens");
        jdbc.update("DELETE FROM users");
        Instant now = Instant.parse("2026-08-20T10:00:00Z");
        jdbc.update("INSERT INTO users(id,email,enabled,created_at,updated_at) VALUES (?,?,?,?,?)",
                1L, "owner@example.com", true, now, now);
        jdbc.update("INSERT INTO users(id,email,enabled,created_at,updated_at) VALUES (?,?,?,?,?)",
                2L, "other@example.com", true, now, now);
        householdId = UUID.randomUUID(); ownerMemberId = UUID.randomUUID(); otherMemberId = UUID.randomUUID();
        jdbc.update("INSERT INTO households(id,name,status,owner_member_id,created_by,created_at,updated_at,version) "
                        + "VALUES (?,?,?,?,?,?,?,0)", householdId, "Casa", "ACTIVE", ownerMemberId, 1L, now, now);
        jdbc.update("INSERT INTO household_members(id,household_id,account_id,role,status,is_owner,joined_at,status_changed_at) "
                        + "VALUES (?,?,?,?,?,?,?,?)", ownerMemberId, householdId, 1L, "ADMIN", "ACTIVE", true, now, now);
        jdbc.update("INSERT INTO household_members(id,household_id,account_id,role,status,is_owner,joined_at,status_changed_at) "
                        + "VALUES (?,?,?,?,?,?,?,?)", otherMemberId, householdId, 2L, "MEMBER", "ACTIVE", false, now, now);
    }

    @Test
    void shouldPersistRehydrateListAndVoidExpense() {
        Expense original = expense();
        Expense saved = expenses.save(original);

        Expense reloaded = expenses.findByIdAndHousehold(saved.id(), saved.householdId()).orElseThrow();
        assertThat(reloaded.total().amount()).isEqualByComparingTo("10.00");
        assertThat(reloaded.split().type()).isEqualTo(ExpenseSplitType.EQUAL);
        assertThat(reloaded.split().allocations()).hasSize(2);

        var page = expenses.findPage(saved.householdId(), LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31), ExpenseStatus.CONFIRMED, 0, 20);
        assertThat(page.items()).singleElement().extracting(Expense::id).isEqualTo(saved.id());

        reloaded.voidExpense(new VoidReason("Duplicated"), Instant.parse("2026-08-20T11:00:00Z"));
        Expense voided = expenses.save(reloaded);
        assertThat(voided.status()).isEqualTo(ExpenseStatus.VOIDED);
        assertThat(voided.version()).isGreaterThan(reloaded.version());
    }

    private Expense expense() {
        Money total = Money.of("10", "EUR");
        var owner = new MemberRef(ownerMemberId); var other = new MemberRef(otherMemberId);
        return Expense.confirmedManual(new ExpenseId(UUID.randomUUID()), new HouseholdRef(householdId), owner,
                owner, total, new ExpenseDescription("Groceries"), LocalDate.of(2026, 8, 20),
                ExpenseSplit.equal(total, List.of(owner, other)), Instant.parse("2026-08-20T10:00:00Z"));
    }
}
