package ncasa.identityaccess.application.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenRepository;
import ncasa.identityaccess.application.port.out.UserAccountRepository;
import ncasa.identityaccess.application.ExpiredEmailVerificationTokenException;
import ncasa.identityaccess.application.InvalidEmailVerificationTokenException;
import ncasa.identityaccess.domain.*;
import org.junit.jupiter.api.Test;

class EmailVerificationUseCasesTest {
    private static final Instant NOW = Instant.parse("2026-09-20T10:00:00Z");
    private static final EmailVerificationTokenHash HASH = new EmailVerificationTokenHash("a".repeat(64));

    @Test
    void shouldConfirmAccountAndInvalidateEveryOtherToken() {
        var account = UserAccount.registered(Email.of("user@example.com"), new PasswordHash("hash"), NOW)
                .withId(new UserId(1L));
        var selected = EmailVerificationToken.create(UUID.randomUUID(), account.id(), HASH, NOW, NOW.plusSeconds(60));
        var other = EmailVerificationToken.create(UUID.randomUUID(), account.id(),
                new EmailVerificationTokenHash("b".repeat(64)), NOW, NOW.plusSeconds(60));
        var users = new FakeUsers(account);
        var tokens = new FakeTokens(List.of(selected, other));
        var useCase = new ConfirmEmailUseCase(users, tokens, raw -> HASH,
                Clock.fixed(NOW.plusSeconds(10), ZoneOffset.UTC));

        useCase.execute("raw-token");

        assertThat(account.canAuthenticate()).isTrue();
        assertThat(selected.consumedAt()).isEqualTo(NOW.plusSeconds(10));
        assertThat(other.invalidatedAt()).isEqualTo(NOW.plusSeconds(10));
        assertThat(users.saved).isSameAs(account);
    }

    @Test
    void shouldRejectExpiredAndUnknownTokens() {
        var account = pendingAccount();
        var expired = EmailVerificationToken.create(UUID.randomUUID(), account.id(), HASH,
                NOW.minusSeconds(120), NOW.minusSeconds(60));
        var useCase = new ConfirmEmailUseCase(new FakeUsers(account), new FakeTokens(List.of(expired)), raw -> HASH,
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertThatThrownBy(() -> useCase.execute("expired"))
                .isInstanceOf(ExpiredEmailVerificationTokenException.class);
        assertThatThrownBy(() -> new ConfirmEmailUseCase(new FakeUsers(account), new FakeTokens(List.of()), raw -> HASH,
                Clock.fixed(NOW, ZoneOffset.UTC)).execute("unknown"))
                .isInstanceOf(InvalidEmailVerificationTokenException.class);
    }

    @Test
    void shouldKeepResendResponseNeutralAndApplyCooldownAndQuota() {
        var account = pendingAccount();
        var users = new FakeUsers(account);
        var tokens = new FakeTokens(new ArrayList<>());
        var events = new ArrayList<EmailVerificationRequested>();
        var issuer = new IssueEmailVerification(tokens, () -> "raw-token", raw -> HASH, events::add,
                Duration.ofHours(24));
        var resend = new ResendEmailVerificationUseCase(users, tokens, issuer,
                Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofMinutes(1), 2);

        resend.execute("missing@example.com");
        resend.execute(account.email().value());
        resend.execute(account.email().value());

        assertThat(events).hasSize(1);
        assertThat(tokens.values).hasSize(1);

        tokens.values.add(EmailVerificationToken.create(UUID.randomUUID(), account.id(),
                new EmailVerificationTokenHash("b".repeat(64)), NOW.minus(Duration.ofHours(2)),
                NOW.plus(Duration.ofHours(22))));
        var afterCooldown = new ResendEmailVerificationUseCase(users, tokens, issuer,
                Clock.fixed(NOW.plus(Duration.ofMinutes(2)), ZoneOffset.UTC), Duration.ofMinutes(1), 2);
        afterCooldown.execute(account.email().value());

        assertThat(events).hasSize(1);
    }

    private UserAccount pendingAccount() {
        return UserAccount.registered(Email.of("user@example.com"), new PasswordHash("hash"), NOW)
                .withId(new UserId(1L));
    }

    private static final class FakeUsers implements UserAccountRepository {
        private final UserAccount account;
        private UserAccount saved;
        private FakeUsers(UserAccount account) { this.account = account; }
        public boolean existsByEmail(Email email) { return account.email().equals(email); }
        public Optional<UserAccount> findByEmail(Email email) { return existsByEmail(email) ? Optional.of(account) : Optional.empty(); }
        public Optional<UserAccount> findById(UserId id) { return account.id().equals(id) ? Optional.of(account) : Optional.empty(); }
        public Optional<UserAccount> findByIdForUpdate(UserId id) { return findById(id); }
        public Map<UserId, Email> findEmailsByIds(Set<UserId> ids) { return Map.of(account.id(), account.email()); }
        public UserAccount save(UserAccount value) { saved = value; return value; }
    }

    private static final class FakeTokens implements EmailVerificationTokenRepository {
        private final List<EmailVerificationToken> values;
        private FakeTokens(List<EmailVerificationToken> values) { this.values = new ArrayList<>(values); }
        public EmailVerificationToken save(EmailVerificationToken token) {
            values.removeIf(existing -> existing.id().equals(token.id()));
            values.add(token);
            return token;
        }
        public List<EmailVerificationToken> saveAll(List<EmailVerificationToken> tokens) { return tokens; }
        public Optional<EmailVerificationToken> findByHash(EmailVerificationTokenHash hash) {
            return values.stream().filter(token -> token.tokenHash().equals(hash)).findFirst();
        }
        public List<EmailVerificationToken> findByUserId(UserId userId) {
            return values.stream().filter(token -> token.userId().equals(userId)).toList();
        }
        public long countCreatedSince(UserId userId, Instant since) {
            return values.stream().filter(token -> token.userId().equals(userId) && !token.createdAt().isBefore(since)).count();
        }
        public Optional<EmailVerificationToken> findLatestByUserId(UserId userId) {
            return findByUserId(userId).stream().max(java.util.Comparator.comparing(EmailVerificationToken::createdAt));
        }
        public long deleteTerminalBefore(Instant cutoff) { return 0; }
    }
}
