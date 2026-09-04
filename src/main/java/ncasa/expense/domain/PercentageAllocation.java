package ncasa.expense.domain;

import java.util.Objects;

public record PercentageAllocation(MemberRef memberId, Percentage percentage) {
    public PercentageAllocation {
        Objects.requireNonNull(memberId, "Member is required");
        Objects.requireNonNull(percentage, "Percentage is required");
    }
}
