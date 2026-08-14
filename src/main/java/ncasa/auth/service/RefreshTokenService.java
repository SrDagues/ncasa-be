package ncasa.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import ncasa.auth.entity.RefreshToken;
import ncasa.auth.repository.RefreshTokenRepository;
import ncasa.common.exception.InvalidRefreshTokenException;
import ncasa.security.JwtProperties;
import ncasa.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {
    private static final int TOKEN_BYTES = 48;
    private final RefreshTokenRepository tokens;
    private final JwtProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository tokens, JwtProperties properties, Clock clock) {
        this.tokens = tokens;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public IssuedRefreshToken create(User user) {
        String rawToken = generateToken();
        RefreshToken entity = new RefreshToken(hash(rawToken), user,
                clock.instant().plus(properties.refreshTokenExpiration()));
        tokens.save(entity);
        return new IssuedRefreshToken(rawToken, entity);
    }

    @Transactional
    public RotationResult rotate(String rawToken) {
        RefreshToken current = requireUsable(rawToken);
        IssuedRefreshToken replacement = create(current.getUser());
        current.revoke(replacement.entity());
        return new RotationResult(current.getUser(), replacement.value());
    }

    @Transactional
    public void revoke(String rawToken, Long authenticatedUserId) {
        RefreshToken token = tokens.findByTokenHash(hash(rawToken)).orElseThrow(InvalidRefreshTokenException::new);
        if (!token.getUser().getId().equals(authenticatedUserId) || token.isRevoked()) {
            throw new InvalidRefreshTokenException();
        }
        token.revoke();
    }

    private RefreshToken requireUsable(String rawToken) {
        RefreshToken token = tokens.findByTokenHash(hash(rawToken)).orElseThrow(InvalidRefreshTokenException::new);
        if (token.isRevoked() || !token.getExpiresAt().isAfter(clock.instant()) || !token.getUser().isEnabled()) {
            throw new InvalidRefreshTokenException();
        }
        return token;
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    public record IssuedRefreshToken(String value, RefreshToken entity) {}
    public record RotationResult(User user, String refreshToken) {}
}
