package ncasa.expense.application.port.out;
import java.util.List; import ncasa.expense.domain.Settlement;
public record SettlementPageSlice(List<Settlement> items,long totalElements){}
