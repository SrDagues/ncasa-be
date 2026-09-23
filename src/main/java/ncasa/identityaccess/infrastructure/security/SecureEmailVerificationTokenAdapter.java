package ncasa.identityaccess.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenGenerator;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenHasher;
import ncasa.identityaccess.domain.EmailVerificationTokenHash;
import org.springframework.stereotype.Component;

@Component
public class SecureEmailVerificationTokenAdapter
        implements EmailVerificationTokenGenerator, EmailVerificationTokenHasher {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public EmailVerificationTokenHash hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) throw new IllegalArgumentException("Verification token is required");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return new EmailVerificationTokenHash(HexFormat.of().formatHex(digest));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
