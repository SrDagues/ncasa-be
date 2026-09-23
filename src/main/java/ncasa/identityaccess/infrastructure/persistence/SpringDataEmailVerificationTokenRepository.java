package ncasa.identityaccess.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataEmailVerificationTokenRepository extends JpaRepository<JpaEmailVerificationTokenEntity, UUID> {
    Optional<JpaEmailVerificationTokenEntity> findByTokenHash(String tokenHash);
    List<JpaEmailVerificationTokenEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndCreatedAtGreaterThanEqual(Long userId, Instant since);
    Optional<JpaEmailVerificationTokenEntity> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("delete from JpaEmailVerificationTokenEntity t where (t.consumedAt is not null or t.invalidatedAt is not null or t.expiresAt < :cutoff) and t.createdAt < :cutoff")
    int deleteTerminalBefore(@Param("cutoff") Instant cutoff);
}
