package ncasa.household.application;

public class HouseholdNotFoundException extends RuntimeException {
    public HouseholdNotFoundException() { super("Household not found"); }
}
