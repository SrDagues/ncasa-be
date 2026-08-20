package ncasa.household.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import ncasa.household.application.port.out.InvitationTokenGenerator;
import ncasa.household.application.port.out.InvitationTokenHasher;
import ncasa.household.domain.InvitationTokenHash;
import org.springframework.stereotype.Component;

@Component
public class SecureInvitationTokenAdapter implements InvitationTokenGenerator, InvitationTokenHasher {
    private final SecureRandom random = new SecureRandom();
    public String generate() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    public InvitationTokenHash hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) throw new IllegalArgumentException("Invitation token is required");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return new InvitationTokenHash(java.util.HexFormat.of().formatHex(digest));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
