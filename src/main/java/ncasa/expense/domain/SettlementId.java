package ncasa.expense.domain;

import java.util.Objects;
import java.util.UUID;

public record SettlementId(UUID value) {
    public SettlementId { Objects.requireNonNull(value); }
    public static SettlementId newId() { return new SettlementId(UUID.randomUUID()); }
}
