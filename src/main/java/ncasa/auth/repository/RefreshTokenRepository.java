package ncasa.auth.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import ncasa.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
