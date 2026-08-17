package ncasa.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserAccountRepository extends JpaRepository<JpaUserAccountEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);
}
