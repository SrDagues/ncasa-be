package ncasa.expense.application.port.out;
import java.util.List;import ncasa.expense.domain.*;
public interface TransactionalOutboxPort { void append(ExpensePlanId planId,List<ExpensePlanEvent> events); }
