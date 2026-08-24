package ncasa.expense.application;

import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import ncasa.expense.application.category.*;
import ncasa.expense.application.classification.*;
import ncasa.expense.application.create.*;
import ncasa.expense.application.draft.*;
import ncasa.expense.application.monthlybalance.GetMonthlyFinancialSummaryUseCase;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpenseStage2UseCasesTest {
    private static final Instant NOW=Instant.parse("2026-08-24T10:00:00Z");
    private static final UUID HOUSEHOLD=UUID.randomUUID(),ACTOR=UUID.randomUUID(),OTHER=UUID.randomUUID();
    private final Clock clock=Clock.fixed(NOW,ZoneOffset.UTC);
    private final InMemoryCategories categories=new InMemoryCategories();
    private final InMemoryDrafts drafts=new InMemoryDrafts();
    private final InMemoryExpenses expenses=new InMemoryExpenses();
    private final InMemoryAudit audit=new InMemoryAudit();
    private HouseholdExpenseAccessPort administrator;

    @BeforeEach void setUp(){administrator=(household,account)->new ExpenseHouseholdContext(household,new MemberRef(ACTOR),true,
            Set.of(new MemberRef(ACTOR),new MemberRef(OTHER)));}

    @Test void shouldCreateCategoryOnlyForAdministratorAndRejectDuplicateName(){
        var useCase=new CreateExpenseCategoryUseCase(categories,administrator,clock);
        var category=useCase.execute(1L,HOUSEHOLD," Food ");
        assertThat(category.name()).isEqualTo("Food");
        assertThatThrownBy(()->useCase.execute(1L,HOUSEHOLD,"food")).isInstanceOf(CategoryConflictException.class);
        HouseholdExpenseAccessPort member=(household,account)->new ExpenseHouseholdContext(household,new MemberRef(ACTOR),false,Set.of(new MemberRef(ACTOR)));
        assertThatThrownBy(()->new CreateExpenseCategoryUseCase(categories,member,clock).execute(1L,HOUSEHOLD,"Travel"))
                .isInstanceOf(ExpenseAccessDeniedException.class);
    }

    @Test void shouldConfirmPercentageDraftOnceAndReplayCreatedExpense(){
        var category=createCategory();
        var draft=new CreateExpenseDraftUseCase(drafts,administrator,clock).execute(1L,HOUSEHOLD);
        var updated=new UpdateExpenseDraftUseCase(drafts,administrator,categories,clock).execute(new UpdateExpenseDraftCommand(1L,HOUSEHOLD,draft.id(),
                "Dinner",new BigDecimal("10.00"),"EUR",LocalDate.of(2026,8,24),ACTOR,category.id(),
                new PercentageSplitCommand(List.of(new PercentageAllocationCommand(ACTOR,new BigDecimal("60.00")),
                        new PercentageAllocationCommand(OTHER,new BigDecimal("40.00")))),draft.version()));
        var confirm=new ConfirmExpenseDraftUseCase(drafts,expenses,categories,administrator,clock);
        var created=confirm.execute(1L,HOUSEHOLD,draft.id(),updated.version());
        var replay=confirm.execute(1L,HOUSEHOLD,draft.id(),updated.version());
        assertThat(created.id()).isEqualTo(replay.id());
        assertThat(created.categoryId()).isEqualTo(category.id());
        assertThat(created.allocations()).extracting(ExpenseView.AllocationView::amount)
                .containsExactlyInAnyOrder(new BigDecimal("6.00"),new BigDecimal("4.00"));
        assertThat(expenses.values).hasSize(1);
    }

    @Test void shouldRejectIncompleteDraftAndStaleUpdate(){
        var draft=new CreateExpenseDraftUseCase(drafts,administrator,clock).execute(1L,HOUSEHOLD);
        var confirm=new ConfirmExpenseDraftUseCase(drafts,expenses,categories,administrator,clock);
        assertThatThrownBy(()->confirm.execute(1L,HOUSEHOLD,draft.id(),draft.version()))
                .isInstanceOf(ExpenseRuleViolationException.class);
        assertThatThrownBy(()->new UpdateExpenseDraftUseCase(drafts,administrator,categories,clock).execute(
                new UpdateExpenseDraftCommand(1L,HOUSEHOLD,draft.id(),null,null,null,null,null,null,null,99)))
                .isInstanceOf(DraftConflictException.class);
    }

    @Test void shouldReclassifyExpenseAndAppendAuditWithoutChangingMoney(){
        var first=createCategory();var second=new CreateExpenseCategoryUseCase(categories,administrator,clock).execute(1L,HOUSEHOLD,"Travel");
        var created=new CreateExpenseUseCase(expenses,administrator,categories,clock).execute(new CreateExpenseCommand(1L,HOUSEHOLD,"Train",new BigDecimal("20"),"EUR",
                LocalDate.of(2026,8,24),ACTOR,first.id(),new EqualSplitCommand(List.of(ACTOR,OTHER))));
        var changed=new ReclassifyExpenseUseCase(expenses,categories,audit,administrator,clock)
                .execute(1L,HOUSEHOLD,created.id(),second.id(),"Corrected");
        assertThat(changed.categoryId()).isEqualTo(second.id());assertThat(changed.amount()).isEqualByComparingTo(created.amount());
        assertThat(audit.values).singleElement().satisfies(item->{assertThat(item.previousCategoryId().value()).isEqualTo(first.id());assertThat(item.newCategoryId().value()).isEqualTo(second.id());});
    }

    @Test void shouldExposeMonthlyCategoryBreakdownWithoutMixingCurrencies(){
        FinancialLedgerReadPort ledger=new FinancialLedgerReadPort(){
            public List<ExpenseLedgerRow> expenseTotals(HouseholdRef h,LocalDate from,LocalDate to){return List.of(
                    new ExpenseLedgerRow("EUR",new MemberRef(ACTOR),new BigDecimal("10.00"),new BigDecimal("5.00")),
                    new ExpenseLedgerRow("EUR",new MemberRef(OTHER),BigDecimal.ZERO,new BigDecimal("5.00")));}
            public List<SettlementLedgerRow> settlementTotals(HouseholdRef h,LocalDate to){return List.of();}
            public List<CategoryExpenseLedgerRow> categoryExpenseTotals(HouseholdRef h,LocalDate from,LocalDate to){return List.of(
                    new CategoryExpenseLedgerRow("EUR",UUID.randomUUID(),"Food",new BigDecimal("10.00")));}
        };
        var result=new GetMonthlyFinancialSummaryUseCase(ledger,administrator).execute(1L,HOUSEHOLD,YearMonth.of(2026,8));
        assertThat(result.currencies()).singleElement().satisfies(currency->{
            assertThat(currency.totalExpenses()).isEqualByComparingTo("10.00");
            assertThat(currency.categories()).singleElement().extracting(item->item.total()).isEqualTo(new BigDecimal("10.00"));
        });
    }

    private ExpenseCategoryView createCategory(){return new CreateExpenseCategoryUseCase(categories,administrator,clock).execute(1L,HOUSEHOLD,"Food");}

    private static final class InMemoryCategories implements ExpenseCategoryRepository{
        private final Map<ExpenseCategoryId,ExpenseCategory> values=new LinkedHashMap<>();
        public ExpenseCategory save(ExpenseCategory c){values.put(c.id(),c);return c;}
        public Optional<ExpenseCategory> findByIdAndHousehold(ExpenseCategoryId id,HouseholdRef h){return Optional.ofNullable(values.get(id)).filter(c->c.householdId().equals(h));}
        public boolean existsActiveByName(HouseholdRef h,CategoryName name,ExpenseCategoryId excluded){return values.values().stream().anyMatch(c->c.householdId().equals(h)&&c.status()==ExpenseCategoryStatus.ACTIVE&&!c.id().equals(excluded)&&c.name().value().equalsIgnoreCase(name.value()));}
        public List<ExpenseCategory> findAll(HouseholdRef h,boolean archived){return values.values().stream().filter(c->c.householdId().equals(h)&&(archived||c.status()==ExpenseCategoryStatus.ACTIVE)).toList();}
    }
    private static final class InMemoryDrafts implements ExpenseDraftRepository{
        private final Map<ExpenseDraftId,ExpenseDraft> values=new LinkedHashMap<>();
        public ExpenseDraft save(ExpenseDraft d){values.put(d.id(),d);return d;}
        public Optional<ExpenseDraft> findByIdAndHousehold(ExpenseDraftId id,HouseholdRef h){return Optional.ofNullable(values.get(id)).filter(d->d.householdId().equals(h));}
        public List<ExpenseDraft> findByCreator(HouseholdRef h,MemberRef creator,ExpenseDraftStatus status){return values.values().stream().filter(d->d.householdId().equals(h)&&d.createdByMemberId().equals(creator)&&(status==null||d.status()==status)).toList();}
    }
    private static final class InMemoryExpenses implements ExpenseRepository{
        private final Map<ExpenseId,Expense> values=new LinkedHashMap<>();
        public Expense save(Expense e){values.put(e.id(),e);return e;}
        public Optional<Expense> findByIdAndHousehold(ExpenseId id,HouseholdRef h){return Optional.ofNullable(values.get(id)).filter(e->e.householdId().equals(h));}
        public ExpensePageSlice findPage(HouseholdRef h,LocalDate from,LocalDate to,ExpenseStatus status,int page,int size){return new ExpensePageSlice(List.copyOf(values.values()),values.size());}
    }
    private static final class InMemoryAudit implements ExpenseClassificationAuditRepository{
        private final List<ExpenseClassificationChange> values=new ArrayList<>();
        public ExpenseClassificationChange save(ExpenseClassificationChange c){values.add(c);return c;}
        public List<ExpenseClassificationChange> findByExpense(ExpenseId id,HouseholdRef h){return values.stream().filter(c->c.expenseId().equals(id)&&c.householdId().equals(h)).toList();}
    }
}
