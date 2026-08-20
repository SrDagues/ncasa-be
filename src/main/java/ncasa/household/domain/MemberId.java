package ncasa.household.domain;

import java.util.UUID;

public record MemberId(UUID value) {
    public MemberId {
        if (value == null) throw new IllegalArgumentException("Member id is required");
    }
}
