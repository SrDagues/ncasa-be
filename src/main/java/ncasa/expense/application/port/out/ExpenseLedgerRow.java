package ncasa.expense.application.port.out;

import java.math.BigDecimal;
import ncasa.expense.domain.MemberRef;

public record ExpenseLedgerRow(String currency, MemberRef memberId, BigDecimal paid, BigDecimal allocated) {}
