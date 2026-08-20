package ncasa.household.domain;

public class HouseholdAccessDeniedException extends RuntimeException {
    public HouseholdAccessDeniedException(String message) { super(message); }
}
