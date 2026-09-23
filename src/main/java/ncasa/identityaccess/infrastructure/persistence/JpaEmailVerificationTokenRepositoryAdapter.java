package ncasa.identityaccess.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenRepository;
import ncasa.identityaccess.domain.EmailVerificationToken;
import ncasa.identityaccess.domain.EmailVerificationTokenHash;
import ncasa.identityaccess.domain.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaEmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepository {
    private final SpringDataEmailVerificationTokenRepository repository;

    public JpaEmailVerificationTokenRepositoryAdapter(SpringDataEmailVerificationTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        var existing = repository.findById(token.id());
        if (existing.isPresent()) {
            existing.orElseThrow().update(token.consumedAt(), token.invalidatedAt());
            repository.save(existing.orElseThrow());
            return token;
        }
        repository.save(toEntity(token));
        return token;
    }

    @Override
    public List<EmailVerificationToken> saveAll(List<EmailVerificationToken> tokens) {
        tokens.forEach(this::save);
        return tokens;
    }

    @Override public Optional<EmailVerificationToken> findByHash(EmailVerificationTokenHash hash) {
        return repository.findByTokenHash(hash.value()).map(this::toDomain);
    }
    @Override public List<EmailVerificationToken> findByUserId(UserId userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId.value()).stream().map(this::toDomain).toList();
    }
    @Override public long countCreatedSince(UserId userId, Instant since) {
        return repository.countByUserIdAndCreatedAtGreaterThanEqual(userId.value(), since);
    }
    @Override public Optional<EmailVerificationToken> findLatestByUserId(UserId userId) {
        return repository.findFirstByUserIdOrderByCreatedAtDesc(userId.value()).map(this::toDomain);
    }
    @Override @Transactional public long deleteTerminalBefore(Instant cutoff) {
        return repository.deleteTerminalBefore(cutoff);
    }

    private JpaEmailVerificationTokenEntity toEntity(EmailVerificationToken token) {
        return new JpaEmailVerificationTokenEntity(token.id(), token.userId().value(), token.tokenHash().value(),
                token.createdAt(), token.expiresAt(), token.consumedAt(), token.invalidatedAt());
    }
    private EmailVerificationToken toDomain(JpaEmailVerificationTokenEntity entity) {
        return EmailVerificationToken.rehydrate(entity.id, new UserId(entity.userId),
                new EmailVerificationTokenHash(entity.tokenHash), entity.createdAt, entity.expiresAt,
                entity.consumedAt, entity.invalidatedAt);
    }
}
