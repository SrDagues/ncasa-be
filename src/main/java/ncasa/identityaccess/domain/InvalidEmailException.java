package ncasa.identityaccess.domain;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException() { super("Invalid email"); }
}
