package ncasa.expense.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;
import ncasa.support.PostgresIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class JpaExpenseStage2RepositoriesIT extends PostgresIntegrationTest {
    @Autowired JdbcTemplate jdbc;@Autowired ExpenseCategoryRepository categories;@Autowired ExpenseDraftRepository drafts;
    @Autowired ExpenseRepository expenses;@Autowired ExpenseClassificationAuditRepository audit;@Autowired FinancialLedgerReadPort ledger;
    private UUID householdId,ownerId,otherId;private final Instant now=Instant.parse("2026-08-24T10:00:00Z");
    @BeforeEach void setUp(){
        jdbc.update("DELETE FROM expense_classification_changes");jdbc.update("DELETE FROM expense_draft_allocations");jdbc.update("DELETE FROM expense_drafts");
        jdbc.update("DELETE FROM expense_allocations");jdbc.update("DELETE FROM settlements");jdbc.update("DELETE FROM expenses");jdbc.update("DELETE FROM expense_categories");
        jdbc.update("DELETE FROM household_invitations");jdbc.update("DELETE FROM household_members");jdbc.update("DELETE FROM households");
        jdbc.update("DELETE FROM auth_identities");jdbc.update("DELETE FROM user_roles");jdbc.update("DELETE FROM refresh_tokens");jdbc.update("DELETE FROM users");
        jdbc.update("INSERT INTO users(id,email,enabled,created_at,updated_at) VALUES (?,?,?,?,?)",1L,"owner-stage2@example.com",true,now,now);
        jdbc.update("INSERT INTO users(id,email,enabled,created_at,updated_at) VALUES (?,?,?,?,?)",2L,"other-stage2@example.com",true,now,now);
        householdId=UUID.randomUUID();ownerId=UUID.randomUUID();otherId=UUID.randomUUID();
        jdbc.update("INSERT INTO households(id,name,status,owner_member_id,created_by,created_at,updated_at,version) VALUES (?,?,?,?,?,?,?,0)",householdId,"Casa","ACTIVE",ownerId,1L,now,now);
        jdbc.update("INSERT INTO household_members(id,household_id,account_id,role,status,is_owner,joined_at,status_changed_at) VALUES (?,?,?,?,?,?,?,?)",ownerId,householdId,1L,"ADMIN","ACTIVE",true,now,now);
        jdbc.update("INSERT INTO household_members(id,household_id,account_id,role,status,is_owner,joined_at,status_changed_at) VALUES (?,?,?,?,?,?,?,?)",otherId,householdId,2L,"MEMBER","ACTIVE",false,now,now);
    }
    @Test void shouldPersistDraftCategoryClassificationAndCategoryLedger(){
        var household=new HouseholdRef(householdId);var owner=new MemberRef(ownerId);var other=new MemberRef(otherId);
        var category=categories.save(ExpenseCategory.create(new ExpenseCategoryId(UUID.randomUUID()),household,owner,new CategoryName("Food"),now));
        var total=Money.of("10.00","EUR");var draft=ExpenseDraft.create(new ExpenseDraftId(UUID.randomUUID()),household,owner,now);
        var percentageSplit=new PercentageDraftSplit(List.of(
                new PercentageAllocation(owner,Percentage.of(new BigDecimal("60.00"))),
                new PercentageAllocation(other,Percentage.of(new BigDecimal("40.00")))));
        draft.update(new ExpenseDraftContent(new ExpenseDescription("Dinner"),owner,total,LocalDate.of(2026,8,24),category.id(),
                percentageSplit),now);
        var reloadedDraft=drafts.findByIdAndHousehold(drafts.save(draft).id(),household).orElseThrow();
        assertThat(reloadedDraft.content().split()).isInstanceOf(PercentageDraftSplit.class);
        var expense=expenses.save(Expense.confirmedManual(new ExpenseId(UUID.randomUUID()),household,owner,owner,total,
                new ExpenseDescription("Dinner"),LocalDate.of(2026,8,24),ExpenseSplit.percentage(total,((PercentageDraftSplit)reloadedDraft.content().split()).allocations()),category.id(),now));
        audit.save(new ExpenseClassificationChange(UUID.randomUUID(),expense.id(),household,owner,null,category.id(),"Classified",now));
        assertThat(audit.findByExpense(expense.id(),household)).hasSize(1);
        assertThat(ledger.categoryExpenseTotals(household,LocalDate.of(2026,8,1),LocalDate.of(2026,8,31)))
                .singleElement().satisfies(row->{assertThat(row.categoryName()).isEqualTo("Food");assertThat(row.total()).isEqualByComparingTo("10.00");});
    }
}
