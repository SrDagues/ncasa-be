package ncasa.identityaccess.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenRepository;
import ncasa.identityaccess.domain.EmailVerificationToken;
import ncasa.identityaccess.domain.EmailVerificationTokenHash;
import ncasa.identityaccess.domain.UserId;
import ncasa.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class JpaEmailVerificationTokenRepositoryAdapterIT extends PostgresIntegrationTest {
    @Autowired EmailVerificationTokenRepository tokens;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM email_verification_tokens");
        jdbc.update("DELETE FROM auth_identities");
        jdbc.update("DELETE FROM user_roles");
        jdbc.update("DELETE FROM users");
        Instant now = Instant.parse("2026-09-20T10:00:00Z");
        jdbc.update("INSERT INTO users(id,email,enabled,created_at,updated_at,email_verified_at) VALUES (?,?,?,?,?,?)",
                1L, "pending@example.com", true, now, now, null);
    }

    @Test
    void shouldPersistFindAndUpdateTokenLifecycle() {
        Instant now = Instant.parse("2026-09-20T10:00:00Z");
        var hash = new EmailVerificationTokenHash("a".repeat(64));
        var token = EmailVerificationToken.create(UUID.randomUUID(), new UserId(1L), hash, now, now.plusSeconds(60));

        tokens.save(token);
        var loaded = tokens.findByHash(hash).orElseThrow();
        loaded.consume(now.plusSeconds(10));
        tokens.save(loaded);

        assertThat(tokens.findByHash(hash).orElseThrow().consumedAt()).isEqualTo(now.plusSeconds(10));
        assertThat(tokens.countCreatedSince(new UserId(1L), now.minusSeconds(1))).isEqualTo(1);
        assertThat(tokens.findLatestByUserId(new UserId(1L))).isPresent();
    }
}
