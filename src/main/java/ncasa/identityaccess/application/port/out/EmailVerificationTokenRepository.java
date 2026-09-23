package ncasa.identityaccess.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import ncasa.identityaccess.domain.EmailVerificationToken;
import ncasa.identityaccess.domain.EmailVerificationTokenHash;
import ncasa.identityaccess.domain.UserId;

public interface EmailVerificationTokenRepository {
    EmailVerificationToken save(EmailVerificationToken token);
    List<EmailVerificationToken> saveAll(List<EmailVerificationToken> tokens);
    Optional<EmailVerificationToken> findByHash(EmailVerificationTokenHash hash);
    List<EmailVerificationToken> findByUserId(UserId userId);
    long countCreatedSince(UserId userId, Instant since);
    Optional<EmailVerificationToken> findLatestByUserId(UserId userId);
    long deleteTerminalBefore(Instant cutoff);
}
