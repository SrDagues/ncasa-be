package ncasa.expense.application.settlement;
import ncasa.expense.application.SettlementView;
public record CreateSettlementResult(SettlementView settlement,boolean replayed){}
