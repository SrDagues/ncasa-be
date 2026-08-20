package ncasa.household.domain;

public class InvitationStateException extends RuntimeException {
    public InvitationStateException(InvitationStatus status) { super("Invitation is " + status); }
}
