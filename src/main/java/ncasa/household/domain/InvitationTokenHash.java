package ncasa.household.domain;

public record InvitationTokenHash(String value) {
    public InvitationTokenHash {
        if (value == null || !value.matches("[a-fA-F0-9]{64}")) {
            throw new IllegalArgumentException("Invitation token hash must be a SHA-256 hex value");
        }
        value = value.toLowerCase();
    }
}
