package ncasa.identityaccess.application;

public class EmailVerificationRequiredException extends RuntimeException {
    public EmailVerificationRequiredException() { super("Email verification required"); }
}
