package ncasa.identityaccess.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAuthIdentityRepository extends JpaRepository<JpaAuthIdentityEntity, Long> {
    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<JpaAuthIdentityEntity> findByProviderAndProviderUserId(String provider, String providerUserId);

    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<JpaAuthIdentityEntity> findByProviderAndUser_Id(String provider, Long userId);
}
