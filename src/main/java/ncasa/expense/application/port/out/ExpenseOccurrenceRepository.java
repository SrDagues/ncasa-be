package ncasa.expense.application.port.out;
import ncasa.expense.domain.*;
public interface ExpenseOccurrenceRepository { boolean exists(ExpensePlanId planId,String occurrenceKey); Expense save(Expense expense); }
