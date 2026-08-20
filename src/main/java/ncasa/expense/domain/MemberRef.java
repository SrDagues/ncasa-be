package ncasa.expense.domain;

import java.util.UUID;

public record MemberRef(UUID value) implements Comparable<MemberRef> {
    public MemberRef {
        if (value == null) throw new IllegalArgumentException("Member reference is required");
    }

    @Override
    public int compareTo(MemberRef other) {
        return value.compareTo(other.value);
    }
}
