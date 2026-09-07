package ncasa.notification.domain;

public record AccountRef(Long value) {
    public AccountRef {
        if (value == null || value <= 0) throw new IllegalArgumentException("Account id must be positive");
    }
}
