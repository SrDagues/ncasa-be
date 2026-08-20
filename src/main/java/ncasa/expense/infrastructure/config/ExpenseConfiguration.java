package ncasa.expense.infrastructure.config;

import java.time.Clock;
import ncasa.expense.application.create.CreateExpenseUseCase;
import ncasa.expense.application.get.GetExpenseUseCase;
import ncasa.expense.application.list.ListExpensesUseCase;
import ncasa.expense.application.port.out.ExpenseRepository;
import ncasa.expense.application.port.out.HouseholdExpenseAccessPort;
import ncasa.expense.application.voidexpense.VoidExpenseUseCase;
import ncasa.expense.application.debt.GetDebtSummaryUseCase;
import ncasa.expense.application.monthlybalance.GetMonthlyFinancialSummaryUseCase;
import ncasa.expense.application.settlement.*;
import ncasa.expense.domain.DebtCalculator;
import ncasa.expense.application.port.out.FinancialLedgerReadPort;
import ncasa.expense.application.port.out.SettlementRepository;
import ncasa.expense.infrastructure.household.HouseholdExpenseAccessAdapter;
import ncasa.household.application.get.GetHouseholdMembershipContextUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpenseConfiguration {
    @Bean HouseholdExpenseAccessPort householdExpenseAccess(GetHouseholdMembershipContextUseCase memberships) {
        return new HouseholdExpenseAccessAdapter(memberships);
    }
    @Bean CreateExpenseUseCase createExpense(ExpenseRepository r, HouseholdExpenseAccessPort h, Clock c) {
        return new CreateExpenseUseCase(r, h, c);
    }
    @Bean GetExpenseUseCase getExpense(ExpenseRepository r, HouseholdExpenseAccessPort h) {
        return new GetExpenseUseCase(r, h);
    }
    @Bean ListExpensesUseCase listExpenses(ExpenseRepository r, HouseholdExpenseAccessPort h) {
        return new ListExpensesUseCase(r, h);
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
}
