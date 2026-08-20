package ncasa.household.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import ncasa.household.application.port.out.HouseholdInvitationRepository;
import ncasa.household.domain.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaHouseholdInvitationRepositoryAdapter implements HouseholdInvitationRepository {
    private final SpringDataHouseholdInvitationRepository repository;
    public JpaHouseholdInvitationRepositoryAdapter(SpringDataHouseholdInvitationRepository repository) { this.repository = repository; }
    public Optional<HouseholdInvitation> findById(InvitationId id) { return repository.findById(id.value()).map(this::toDomain); }
    public Optional<HouseholdInvitation> findByTokenHash(InvitationTokenHash hash) { return repository.findByTokenHash(hash.value()).map(this::toDomain); }
    public Optional<HouseholdInvitation> findPending(HouseholdId id, InvitationEmail email) {
        return repository.findFirstByHouseholdIdAndEmailAndStatus(id.value(), email.value(), InvitationStatus.PENDING.name()).map(this::toDomain);
    }
    public List<HouseholdInvitation> findPendingByEmail(InvitationEmail email) {
        return repository.findAllByEmailAndStatusOrderByCreatedAtDesc(email.value(), InvitationStatus.PENDING.name())
                .stream().map(this::toDomain).toList();
    }
    public List<HouseholdInvitation> findPendingByHousehold(HouseholdId householdId) {
        return repository.findAllByHouseholdIdAndStatusOrderByCreatedAtDesc(
                householdId.value(), InvitationStatus.PENDING.name()).stream().map(this::toDomain).toList();
    }
    public HouseholdInvitation save(HouseholdInvitation invitation) { return toDomain(repository.saveAndFlush(toEntity(invitation))); }
    private JpaHouseholdInvitationEntity toEntity(HouseholdInvitation invitation) {
        return new JpaHouseholdInvitationEntity(invitation.id().value(), invitation.householdId().value(),
                invitation.email().value(), invitation.invitedRole().name(), invitation.invitedBy().value(),
                invitation.tokenHash().value(), invitation.status().name(), invitation.createdAt(),
                invitation.expiry().value(), invitation.statusChangedAt());
    }
    private HouseholdInvitation toDomain(JpaHouseholdInvitationEntity entity) {
        return HouseholdInvitation.rehydrate(new InvitationId(entity.id()), new HouseholdId(entity.householdId()),
                InvitationEmail.of(entity.email()), HouseholdRole.valueOf(entity.invitedRole()), new MemberId(entity.invitedBy()),
                new InvitationTokenHash(entity.tokenHash()), entity.createdAt(),
                new InvitationExpiry(entity.createdAt(), entity.expiresAt()), InvitationStatus.valueOf(entity.status()),
                entity.statusChangedAt());
    }
}
