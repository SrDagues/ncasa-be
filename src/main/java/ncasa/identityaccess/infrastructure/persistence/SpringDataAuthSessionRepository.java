package ncasa.identityaccess.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

interface SpringDataAuthSessionRepository extends JpaRepository<JpaAuthSessionEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<JpaAuthSessionEntity> findByTokenHash(String tokenHash);
    List<JpaAuthSessionEntity> findAllByUserIdAndRevokedFalse(Long userId);
}
