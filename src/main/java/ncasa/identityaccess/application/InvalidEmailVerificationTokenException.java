package ncasa.identityaccess.application;

public class InvalidEmailVerificationTokenException extends RuntimeException {
    public InvalidEmailVerificationTokenException() { super("Invalid email verification token"); }
}
