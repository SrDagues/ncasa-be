package ncasa.identityaccess.domain;

public class EmailAlreadyVerifiedException extends RuntimeException {
    public EmailAlreadyVerifiedException() { super("Email is already verified"); }
}
