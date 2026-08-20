package ncasa.household.domain;

import java.util.Locale;
import java.util.regex.Pattern;

public record InvitationEmail(String value) {
    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public InvitationEmail {
        if (value == null) throw new IllegalArgumentException("Invitation email is required");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 320 || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid invitation email");
        }
    }

    public static InvitationEmail of(String value) {
        return new InvitationEmail(value);
    }
}
