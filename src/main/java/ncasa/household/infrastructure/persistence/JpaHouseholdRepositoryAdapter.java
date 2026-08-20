package ncasa.household.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaHouseholdRepositoryAdapter implements HouseholdRepository {
    private final SpringDataHouseholdRepository repository;
    public JpaHouseholdRepositoryAdapter(SpringDataHouseholdRepository repository) { this.repository = repository; }

    public Optional<Household> findById(HouseholdId id) { return repository.findById(id.value()).map(this::toDomain); }
    public List<Household> findActiveByMemberAccountId(AccountId accountId) {
        return repository.findActiveByMemberAccountId(accountId.value()).stream().map(this::toDomain).toList();
    }
    public Household save(Household household) { return toDomain(repository.saveAndFlush(toEntity(household))); }

    private JpaHouseholdEntity toEntity(Household household) {
        var entity = new JpaHouseholdEntity(household.id().value(), household.name().value(), household.status().name(),
                household.ownerMemberId().value(), household.createdBy().value(), household.createdAt(),
                household.updatedAt(), household.version());
        household.members().forEach(member -> entity.addMember(new JpaHouseholdMemberEntity(member.id().value(),
                member.accountId().value(), member.role().name(), member.status().name(),
                member.id().equals(household.ownerMemberId()), member.joinedAt(), member.statusChangedAt())));
        return entity;
    }

    private Household toDomain(JpaHouseholdEntity entity) {
        var members = entity.members().stream().map(member -> HouseholdMember.rehydrate(new MemberId(member.id()),
                new AccountId(member.accountId()), HouseholdRole.valueOf(member.role()),
                MembershipStatus.valueOf(member.status()), member.joinedAt(), member.statusChangedAt())).toList();
        return Household.rehydrate(new HouseholdId(entity.id()), HouseholdName.of(entity.name()),
                HouseholdStatus.valueOf(entity.status()), new MemberId(entity.ownerMemberId()), members,
                new AccountId(entity.createdBy()), entity.createdAt(), entity.updatedAt(), entity.version());
    }
}
