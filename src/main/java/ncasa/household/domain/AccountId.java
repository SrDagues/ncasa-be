package ncasa.household.domain;

public record AccountId(Long value) {
    public AccountId {
        if (value == null || value <= 0) throw new IllegalArgumentException("Account id must be positive");
    }
}
