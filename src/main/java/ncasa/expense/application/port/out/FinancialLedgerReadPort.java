package ncasa.expense.application.port.out;

import java.time.LocalDate;
import java.util.List;
import ncasa.expense.domain.HouseholdRef;

public interface FinancialLedgerReadPort {
    List<ExpenseLedgerRow> expenseTotals(HouseholdRef householdId, LocalDate fromInclusive, LocalDate toInclusive);
    List<SettlementLedgerRow> settlementTotals(HouseholdRef householdId, LocalDate toInclusive);
}
