package ncasa.expense.application;
import java.util.List;
public record ExpensePlanPage(List<ExpensePlanView> content,int page,int size,long totalElements,int totalPages){public ExpensePlanPage{content=List.copyOf(content);}}
