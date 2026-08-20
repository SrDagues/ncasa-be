package ncasa.expense.application.create;

import java.math.BigDecimal;
import java.util.UUID;

public record ExactAllocationCommand(UUID memberId, BigDecimal amount) {}
