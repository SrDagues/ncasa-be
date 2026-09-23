package ncasa.identityaccess.application;

public record RegistrationResult(String status) {
    public static RegistrationResult pendingEmailVerification() {
        return new RegistrationResult("PENDING_EMAIL_VERIFICATION");
    }
}
