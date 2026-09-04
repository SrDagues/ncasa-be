package ncasa.expense.application.plan;
import java.math.BigDecimal;import java.util.*;import ncasa.expense.domain.ExpenseSplitType;
public record PlanSplitCommand(ExpenseSplitType type,List<UUID> memberIds,List<Allocation> allocations){
 public record Allocation(UUID memberId,BigDecimal amount,BigDecimal percentage){}
}
