package ncasa.household.domain;

public class InvitationExpiredException extends RuntimeException {
    public InvitationExpiredException() { super("Invitation has expired"); }
}
