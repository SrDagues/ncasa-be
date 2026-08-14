package ncasa.user.repository;

import java.util.Optional;
import ncasa.user.entity.AuthIdentity;
import ncasa.user.entity.AuthProvider;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, Long> {
    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
