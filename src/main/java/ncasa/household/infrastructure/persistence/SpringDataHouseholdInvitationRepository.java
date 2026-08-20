package ncasa.household.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataHouseholdInvitationRepository extends JpaRepository<JpaHouseholdInvitationEntity, UUID> {
    Optional<JpaHouseholdInvitationEntity> findByTokenHash(String tokenHash);
    Optional<JpaHouseholdInvitationEntity> findFirstByHouseholdIdAndEmailAndStatus(UUID householdId, String email, String status);
    List<JpaHouseholdInvitationEntity> findAllByEmailAndStatusOrderByCreatedAtDesc(String email, String status);
    List<JpaHouseholdInvitationEntity> findAllByHouseholdIdAndStatusOrderByCreatedAtDesc(UUID householdId, String status);
}
