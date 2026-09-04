package ncasa.expense.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public record ExpensePlanEvent(String type, String deduplicationKey, Instant occurredAt,
        LocalDate occurrenceDate, Map<String, Object> attributes) {
    public ExpensePlanEvent { attributes = Map.copyOf(attributes == null ? Map.of() : attributes); }
}
