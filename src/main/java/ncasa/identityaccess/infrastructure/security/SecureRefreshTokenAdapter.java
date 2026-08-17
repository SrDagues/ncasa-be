package ncasa.identityaccess.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import ncasa.identityaccess.application.port.out.RefreshTokenGenerator;
import ncasa.identityaccess.application.port.out.TokenHasher;
import ncasa.identityaccess.domain.RefreshTokenHash;
import org.springframework.stereotype.Component;

@Component
public class SecureRefreshTokenAdapter implements RefreshTokenGenerator, TokenHasher {
    private static final int TOKEN_BYTES = 48;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public RefreshTokenHash hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return new RefreshTokenHash(HexFormat.of().formatHex(digest));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
