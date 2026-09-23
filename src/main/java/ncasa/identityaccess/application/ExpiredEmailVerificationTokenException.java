package ncasa.identityaccess.application;

public class ExpiredEmailVerificationTokenException extends RuntimeException {
    public ExpiredEmailVerificationTokenException() { super("Email verification token has expired"); }
}
