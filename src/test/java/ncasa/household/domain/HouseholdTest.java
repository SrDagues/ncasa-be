package ncasa.household.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HouseholdTest {
    private static final Instant NOW = Instant.parse("2026-08-20T10:00:00Z");
    private final AccountId creatorAccount = new AccountId(1L);
    private final MemberId creatorMember = new MemberId(UUID.randomUUID());
    private Household household;

    @BeforeEach
    void setUp() {
        household = Household.create(new HouseholdId(UUID.randomUUID()), HouseholdName.of("Casa Azul"),
                creatorMember, creatorAccount, NOW);
    }

    @Test
    void shouldCreateHouseholdWithCreatorAsActiveAdminAndOwner() {
        assertThat(household.ownerMemberId()).isEqualTo(creatorMember);
        assertThat(household.createdBy()).isEqualTo(creatorAccount);
        assertThat(household.status()).isEqualTo(HouseholdStatus.ACTIVE);
        assertThat(household.members()).singleElement().satisfies(member -> {
            assertThat(member.accountId()).isEqualTo(creatorAccount);
            assertThat(member.role()).isEqualTo(HouseholdRole.ADMIN);
            assertThat(member.status()).isEqualTo(MembershipStatus.ACTIVE);
        });
    }

    @Test
    void shouldTransferOwnershipAndKeepFormerOwnerAsAdmin() {
        MemberId memberId = new MemberId(UUID.randomUUID());
        household.addOrReactivateMember(memberId, new AccountId(2L), HouseholdRole.MEMBER, NOW.plusSeconds(1));

        household.transferOwnership(creatorMember, memberId, NOW.plusSeconds(2));

        assertThat(household.ownerMemberId()).isEqualTo(memberId);
        assertThat(household.member(memberId).role()).isEqualTo(HouseholdRole.ADMIN);
        assertThat(household.member(creatorMember).role()).isEqualTo(HouseholdRole.ADMIN);
    }

    @Test
    void shouldProtectOwnerAndAdministratorRules() {
        MemberId admin = new MemberId(UUID.randomUUID());
        MemberId member = new MemberId(UUID.randomUUID());
        household.addOrReactivateMember(admin, new AccountId(2L), HouseholdRole.ADMIN, NOW.plusSeconds(1));
        household.addOrReactivateMember(member, new AccountId(3L), HouseholdRole.MEMBER, NOW.plusSeconds(1));

        assertThatThrownBy(() -> household.leave(creatorMember, NOW.plusSeconds(2)))
                .isInstanceOf(HouseholdRuleViolationException.class);
        assertThatThrownBy(() -> household.removeMember(admin, creatorMember, NOW.plusSeconds(2)))
                .isInstanceOf(HouseholdRuleViolationException.class);
        assertThatThrownBy(() -> household.removeMember(admin, admin, NOW.plusSeconds(2)))
                .isInstanceOf(HouseholdAccessDeniedException.class);

        household.removeMember(admin, member, NOW.plusSeconds(3));
        assertThat(household.member(member).status()).isEqualTo(MembershipStatus.REMOVED);
    }

    @Test
    void shouldReactivateExistingMembershipWithoutChangingIdentityOrFirstJoinDate() {
        MemberId memberId = new MemberId(UUID.randomUUID());
        AccountId accountId = new AccountId(2L);
        household.addOrReactivateMember(memberId, accountId, HouseholdRole.MEMBER, NOW.plusSeconds(1));
        household.leave(memberId, NOW.plusSeconds(2));

        HouseholdMember reactivated = household.addOrReactivateMember(new MemberId(UUID.randomUUID()), accountId,
                HouseholdRole.ADMIN, NOW.plusSeconds(3));

        assertThat(reactivated.id()).isEqualTo(memberId);
        assertThat(reactivated.joinedAt()).isEqualTo(NOW.plusSeconds(1));
        assertThat(reactivated.status()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(reactivated.role()).isEqualTo(HouseholdRole.ADMIN);
    }

    @Test
    void shouldOnlyArchiveWhenOwnerIsOnlyActiveMember() {
        MemberId memberId = new MemberId(UUID.randomUUID());
        household.addOrReactivateMember(memberId, new AccountId(2L), HouseholdRole.MEMBER, NOW.plusSeconds(1));
        assertThatThrownBy(() -> household.archive(creatorMember, NOW.plusSeconds(2)))
                .isInstanceOf(HouseholdRuleViolationException.class);

        household.removeMember(creatorMember, memberId, NOW.plusSeconds(3));
        household.archive(creatorMember, NOW.plusSeconds(4));

        assertThat(household.status()).isEqualTo(HouseholdStatus.ARCHIVED);
        assertThatThrownBy(() -> household.rename(creatorMember, HouseholdName.of("Otra"), NOW.plusSeconds(5)))
                .isInstanceOf(HouseholdRuleViolationException.class);
    }
}
