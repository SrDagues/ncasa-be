package ncasa.expense.application.port.out;

import java.math.BigDecimal;
import ncasa.expense.domain.MemberRef;

public record SettlementLedgerRow(String currency, MemberRef memberId, BigDecimal settledOut, BigDecimal settledIn) {}
