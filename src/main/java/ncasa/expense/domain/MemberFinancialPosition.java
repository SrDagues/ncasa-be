package ncasa.expense.domain;

import java.util.Objects;

public record MemberFinancialPosition(MemberRef memberId, Money net) {
    public MemberFinancialPosition { Objects.requireNonNull(memberId); Objects.requireNonNull(net); }
}
