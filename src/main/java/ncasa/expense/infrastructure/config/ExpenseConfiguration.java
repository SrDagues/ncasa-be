package ncasa.expense.infrastructure.config;

import java.time.Clock;
import ncasa.expense.application.create.CreateExpenseUseCase;
import ncasa.expense.application.category.*;
import ncasa.expense.application.draft.*;
import ncasa.expense.application.classification.*;
import ncasa.expense.application.get.GetExpenseUseCase;
import ncasa.expense.application.list.ListExpensesUseCase;
import ncasa.expense.application.port.out.ExpenseRepository;
import ncasa.expense.application.port.out.ExpenseCategoryRepository;
import ncasa.expense.application.port.out.ExpenseDraftRepository;
import ncasa.expense.application.port.out.ExpenseClassificationAuditRepository;
import ncasa.expense.application.port.out.HouseholdExpenseAccessPort;
import ncasa.expense.application.voidexpense.VoidExpenseUseCase;
import ncasa.expense.application.debt.GetDebtSummaryUseCase;
import ncasa.expense.application.monthlybalance.GetMonthlyFinancialSummaryUseCase;
import ncasa.expense.application.settlement.*;
import ncasa.expense.application.plan.*;
import ncasa.expense.application.forecast.*;
import ncasa.expense.application.occurrence.*;
import ncasa.expense.application.reminder.*;
import ncasa.expense.domain.DebtCalculator;
import ncasa.expense.application.port.out.FinancialLedgerReadPort;
import ncasa.expense.application.port.out.SettlementRepository;
import ncasa.expense.application.port.out.ExpensePlanRepository;
import ncasa.expense.application.port.out.ExpenseOccurrenceRepository;
import ncasa.expense.application.port.out.ExpensePlanHouseholdStatePort;
import ncasa.expense.application.port.out.TransactionalOutboxPort;
import ncasa.expense.application.port.out.OutboxMessageRepository;
import ncasa.expense.application.port.out.DomainEventPublisherPort;
import ncasa.expense.infrastructure.household.HouseholdExpenseAccessAdapter;
import ncasa.household.application.get.GetHouseholdMembershipContextUseCase;
import ncasa.household.application.get.GetHouseholdStateForExpensePlansUseCase;
import ncasa.expense.infrastructure.household.ExpensePlanHouseholdStateAdapter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({ExpensePlanProperties.class,OutboxProperties.class})
public class ExpenseConfiguration {
    @Bean HouseholdExpenseAccessPort householdExpenseAccess(GetHouseholdMembershipContextUseCase memberships) {
        return new HouseholdExpenseAccessAdapter(memberships);
    }
    @Bean CreateExpenseUseCase createExpense(ExpenseRepository r, HouseholdExpenseAccessPort h,
            ExpenseCategoryRepository categories, Clock c) {
        return new CreateExpenseUseCase(r, h, categories, c);
    }
    @Bean GetExpenseUseCase getExpense(ExpenseRepository r, HouseholdExpenseAccessPort h) {
        return new GetExpenseUseCase(r, h);
    }
    @Bean ListExpensesUseCase listExpenses(ExpenseRepository r, HouseholdExpenseAccessPort h,ExpenseCategoryRepository categories) {
        return new ListExpensesUseCase(r, h,categories);
    }
    @Bean VoidExpenseUseCase voidExpense(ExpenseRepository r, HouseholdExpenseAccessPort h, Clock c) {
        return new VoidExpenseUseCase(r, h, c);
    }
    @Bean DebtCalculator debtCalculator() { return new DebtCalculator(); }
    @Bean GetMonthlyFinancialSummaryUseCase monthlySummary(FinancialLedgerReadPort l, HouseholdExpenseAccessPort h) { return new GetMonthlyFinancialSummaryUseCase(l,h); }
    @Bean GetDebtSummaryUseCase debtSummary(FinancialLedgerReadPort l, HouseholdExpenseAccessPort h, Clock c, DebtCalculator d) { return new GetDebtSummaryUseCase(l,h,c,d); }
    @Bean CreateSettlementUseCase createSettlement(SettlementRepository r,HouseholdExpenseAccessPort h,GetDebtSummaryUseCase d,Clock c){return new CreateSettlementUseCase(r,h,d,c);}
    @Bean GetSettlementUseCase getSettlement(SettlementRepository r,HouseholdExpenseAccessPort h){return new GetSettlementUseCase(r,h);}
    @Bean ListSettlementsUseCase listSettlements(SettlementRepository r,HouseholdExpenseAccessPort h){return new ListSettlementsUseCase(r,h);}
    @Bean VoidSettlementUseCase voidSettlement(SettlementRepository r,HouseholdExpenseAccessPort h,Clock c){return new VoidSettlementUseCase(r,h,c);}
    @Bean CreateExpenseCategoryUseCase createExpenseCategory(ExpenseCategoryRepository r,HouseholdExpenseAccessPort h,Clock c){return new CreateExpenseCategoryUseCase(r,h,c);}
    @Bean RenameExpenseCategoryUseCase renameExpenseCategory(ExpenseCategoryRepository r,HouseholdExpenseAccessPort h,Clock c){return new RenameExpenseCategoryUseCase(r,h,c);}
    @Bean ArchiveExpenseCategoryUseCase archiveExpenseCategory(ExpenseCategoryRepository r,HouseholdExpenseAccessPort h,Clock c){return new ArchiveExpenseCategoryUseCase(r,h,c);}
    @Bean ListExpenseCategoriesUseCase listExpenseCategories(ExpenseCategoryRepository r,HouseholdExpenseAccessPort h){return new ListExpenseCategoriesUseCase(r,h);}
    @Bean CreateExpenseDraftUseCase createExpenseDraft(ExpenseDraftRepository r,HouseholdExpenseAccessPort h,Clock c){return new CreateExpenseDraftUseCase(r,h,c);}
    @Bean GetExpenseDraftUseCase getExpenseDraft(ExpenseDraftRepository r,HouseholdExpenseAccessPort h){return new GetExpenseDraftUseCase(r,h);}
    @Bean ListExpenseDraftsUseCase listExpenseDrafts(ExpenseDraftRepository r,HouseholdExpenseAccessPort h){return new ListExpenseDraftsUseCase(r,h);}
    @Bean UpdateExpenseDraftUseCase updateExpenseDraft(ExpenseDraftRepository r,HouseholdExpenseAccessPort h,ExpenseCategoryRepository categories,Clock c){return new UpdateExpenseDraftUseCase(r,h,categories,c);}
    @Bean DiscardExpenseDraftUseCase discardExpenseDraft(ExpenseDraftRepository r,HouseholdExpenseAccessPort h,Clock c){return new DiscardExpenseDraftUseCase(r,h,c);}
    @Bean ConfirmExpenseDraftUseCase confirmExpenseDraft(ExpenseDraftRepository d,ExpenseRepository e,ExpenseCategoryRepository categories,HouseholdExpenseAccessPort h,Clock c){return new ConfirmExpenseDraftUseCase(d,e,categories,h,c);}
    @Bean ReclassifyExpenseUseCase reclassifyExpense(ExpenseRepository e,ExpenseCategoryRepository categories,ExpenseClassificationAuditRepository audit,HouseholdExpenseAccessPort h,Clock c){return new ReclassifyExpenseUseCase(e,categories,audit,h,c);}
    @Bean ListExpenseClassificationHistoryUseCase classificationHistory(ExpenseRepository e,ExpenseClassificationAuditRepository audit,HouseholdExpenseAccessPort h){return new ListExpenseClassificationHistoryUseCase(e,audit,h);}
    @Bean ExpensePlanHouseholdStatePort expensePlanHouseholdState(GetHouseholdStateForExpensePlansUseCase h,ExpenseCategoryRepository c){return new ExpensePlanHouseholdStateAdapter(h,c);}
    @Bean CreateExpensePlanUseCase createExpensePlan(ExpensePlanRepository p,HouseholdExpenseAccessPort h,ExpenseCategoryRepository c,TransactionalOutboxPort o,Clock k){return new CreateExpensePlanUseCase(p,h,c,o,k);}
    @Bean GetExpensePlanNotificationContextUseCase expensePlanNotificationContext(ExpensePlanRepository p){return new GetExpensePlanNotificationContextUseCase(p);}
    @Bean GetExpensePlanUseCase getExpensePlan(ExpensePlanRepository p,HouseholdExpenseAccessPort h){return new GetExpensePlanUseCase(p,h);}
    @Bean ListExpensePlansUseCase listExpensePlans(ExpensePlanRepository p,HouseholdExpenseAccessPort h){return new ListExpensePlansUseCase(p,h);}
    @Bean ExpensePlanLifecycleUseCase expensePlanLifecycle(ExpensePlanRepository p,HouseholdExpenseAccessPort h,TransactionalOutboxPort o,Clock c){return new ExpensePlanLifecycleUseCase(p,h,o,c);}
    @Bean ForecastExpensePlansUseCase forecastExpensePlans(ExpensePlanRepository p,HouseholdExpenseAccessPort h,Clock c){return new ForecastExpensePlansUseCase(p,h,c);}
    @Bean MaterializeDueExpensePlanUseCase materializeDueExpensePlan(ExpensePlanRepository p,ExpenseOccurrenceRepository e,ExpensePlanHouseholdStatePort h,TransactionalOutboxPort o,Clock c){return new MaterializeDueExpensePlanUseCase(p,e,h,o,c);}
    @Bean PublishDueExpensePlanReminderUseCase publishExpensePlanReminder(ExpensePlanRepository p,TransactionalOutboxPort o,Clock c){return new PublishDueExpensePlanReminderUseCase(p,o,c);}
    @Bean DispatchOutboxMessagesUseCase dispatchOutbox(OutboxMessageRepository r,DomainEventPublisherPort p,Clock c,OutboxProperties x){return new DispatchOutboxMessagesUseCase(r,p,c,x.batchSize(),x.maxAttempts());}
}
