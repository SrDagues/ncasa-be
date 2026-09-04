package ncasa.notification.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record NotificationAmount(BigDecimal amount, String currency) {
    public NotificationAmount {
        Objects.requireNonNull(amount, "Amount is required");
        if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency must contain three uppercase letters");
        }
        Currency resolved;
        try { resolved=Currency.getInstance(currency); }
        catch(IllegalArgumentException ex){throw new IllegalArgumentException("Unknown currency: "+currency);}
        int fractionDigits=resolved.getDefaultFractionDigits();
        if(fractionDigits<0||fractionDigits>4)throw new IllegalArgumentException("Unsupported currency fraction digits: "+currency);
        if(Math.max(amount.stripTrailingZeros().scale(),0)>fractionDigits)throw new IllegalArgumentException("Amount has too many fraction digits for "+currency);
        amount=amount.setScale(fractionDigits);
        if(amount.precision()-amount.scale()>15)throw new IllegalArgumentException("Amount exceeds supported precision");
    }
}
