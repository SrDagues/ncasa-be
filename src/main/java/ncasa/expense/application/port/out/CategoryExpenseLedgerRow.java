package ncasa.expense.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryExpenseLedgerRow(String currency,UUID categoryId,String categoryName,BigDecimal total){}
