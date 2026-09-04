package ncasa.expense.application.port.out;
import java.util.List;import ncasa.expense.domain.ExpensePlan;
public record ExpensePlanPageSlice(List<ExpensePlan> content,long totalElements){public ExpensePlanPageSlice{content=List.copyOf(content);}}
