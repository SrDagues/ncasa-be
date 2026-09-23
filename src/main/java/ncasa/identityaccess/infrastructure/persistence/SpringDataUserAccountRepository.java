package ncasa.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface SpringDataUserAccountRepository extends JpaRepository<JpaUserAccountEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from JpaUserAccountEntity u where u.id = :id")
    Optional<JpaUserAccountEntity> findForUpdateById(@Param("id") Long id);
}
