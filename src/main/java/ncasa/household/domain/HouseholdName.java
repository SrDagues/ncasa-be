package ncasa.household.domain;

public record HouseholdName(String value) {
    public static final int MAX_LENGTH = 120;

    public HouseholdName {
        if (value == null) throw new IllegalArgumentException("Household name is required");
        value = value.trim().replaceAll("\\s+", " ");
        if (value.isBlank()) throw new IllegalArgumentException("Household name is required");
        if (value.length() > MAX_LENGTH) throw new IllegalArgumentException("Household name is too long");
    }

    public static HouseholdName of(String value) {
        return new HouseholdName(value);
    }
}
