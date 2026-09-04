package ncasa.expense.application;
import java.util.List;
public record SettlementPage(List<SettlementView> items,int page,int size,long totalElements,int totalPages){}
