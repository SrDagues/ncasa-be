package ncasa.expense.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.*;
import java.util.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;
import ncasa.support.PostgresIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class JpaExpensePlanRepositoryAdapterIT extends PostgresIntegrationTest {
    @Autowired ExpensePlanRepository plans;
    @Autowired TransactionalOutboxPort outbox;
    @Autowired JdbcTemplate jdbc;
    private UUID householdId;
    private UUID memberId;
    private final Instant now = Instant.parse("2026-08-25T10:00:00Z");

    @BeforeEach void setUp() {
        jdbc.update("DELETE FROM outbox_messages");
        jdbc.update("DELETE FROM expense_plan_allocations");
        jdbc.update("DELETE FROM expense_plans");
        jdbc.update("DELETE FROM household_invitations");
        jdbc.update("DELETE FROM household_members");
        jdbc.update("DELETE FROM households");
        jdbc.update("DELETE FROM auth_identities");
        jdbc.update("DELETE FROM user_roles");
        jdbc.update("DELETE FROM refresh_tokens");
        jdbc.update("DELETE FROM users");
        jdbc.update("INSERT INTO users(id,email,enabled,created_at,updated_at) VALUES (?,?,?,?,?)",
                1L, "plan-owner@example.com", true, now, now);
        householdId = UUID.randomUUID(); memberId = UUID.randomUUID();
        jdbc.update("INSERT INTO households(id,name,status,owner_member_id,created_by,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)",
                householdId, "Plan house", "ACTIVE", memberId, 1L, now, now);
        jdbc.update("INSERT INTO household_members(id,household_id,account_id,role,status,is_owner,joined_at,status_changed_at) VALUES (?,?,?,?,?,?,?,?)",
                memberId, householdId, 1L, "ADMIN", "ACTIVE", true, now, now);
    }

    @Test void persistsAndRehydratesPlanAndTransactionalEvents() {
        var member = new MemberRef(memberId);
        var plan = ExpensePlan.create(new ExpensePlanId(UUID.randomUUID()), new HouseholdRef(householdId), member,
                new ExpenseTemplate(new ExpenseDescription("Rent"), Money.of("900", "EUR"), member, null,
                        new EqualTemplateSplit(List.of(member))),
                Schedule.monthly(LocalDate.of(2026, 9, 1)), EndCondition.afterOccurrences(3),
                ZoneId.of("Europe/Madrid"), 1, now);
        var events = plan.pullEvents();

        var saved = plans.save(plan);
        outbox.append(saved.id(), events);
        var reloaded = plans.findByIdAndHousehold(saved.id(), new HouseholdRef(householdId)).orElseThrow();

        assertThat(reloaded.nextOccurrence()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(reloaded.endCondition().occurrenceLimit()).isEqualTo(3);
        assertThat(reloaded.template().split()).isInstanceOf(EqualTemplateSplit.class);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM outbox_messages WHERE aggregate_id=?",
                Long.class, saved.id().value())).isEqualTo(1L);
    }
}
