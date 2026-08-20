package ncasa.expense.infrastructure.config;

import java.time.Clock;
import ncasa.expense.application.create.CreateExpenseUseCase;
import ncasa.expense.application.get.GetExpenseUseCase;
import ncasa.expense.application.list.ListExpensesUseCase;
import ncasa.expense.application.port.out.ExpenseRepository;
import ncasa.expense.application.port.out.HouseholdExpenseAccessPort;
import ncasa.expense.application.voidexpense.VoidExpenseUseCase;
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
}
