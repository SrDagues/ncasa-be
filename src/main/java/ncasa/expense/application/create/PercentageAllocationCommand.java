package ncasa.expense.application.create;

import java.math.BigDecimal;
import java.util.UUID;

public record PercentageAllocationCommand(UUID memberId, BigDecimal percentage) {}
