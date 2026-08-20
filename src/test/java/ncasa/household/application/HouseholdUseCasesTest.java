package ncasa.household.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ncasa.household.application.accept.AcceptInvitationByIdUseCase;
import ncasa.household.application.accept.InvitationAcceptanceService;
import ncasa.household.application.create.CreateHouseholdUseCase;
import ncasa.household.application.get.ListAccountHouseholdsUseCase;
import ncasa.household.application.invite.GetHouseholdPendingInvitationsUseCase;
import ncasa.household.application.invite.InviteHouseholdMemberUseCase;
import ncasa.household.application.port.out.HouseholdInvitationRepository;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.application.port.out.InvitationDeliveryPort;
import ncasa.household.application.port.out.InvitationTokenGenerator;
import ncasa.household.application.port.out.InvitationTokenHasher;
import ncasa.household.domain.AccountId;
import ncasa.household.domain.Household;
import ncasa.household.domain.HouseholdId;
import ncasa.household.domain.HouseholdInvitation;
import ncasa.household.domain.HouseholdRole;
import ncasa.household.domain.InvitationEmail;
import ncasa.household.domain.InvitationId;
import ncasa.household.domain.InvitationStatus;
import ncasa.household.domain.InvitationTokenHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HouseholdUseCasesTest {
    private static final Instant NOW = Instant.parse("2026-08-20T10:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private InMemoryHouseholds households;
    private InMemoryInvitations invitations;
    private HouseholdViewAssembler views;

    @BeforeEach
    void setUp() {
        households = new InMemoryHouseholds();
        invitations = new InMemoryInvitations();
        views = new HouseholdViewAssembler(ids -> ids.stream().collect(java.util.stream.Collectors.toMap(
                id -> id, id -> "account-" + id.value() + "@example.com")));
    }

    @Test
    void shouldCreateHouseholdForAuthenticatedAccount() {
        var useCase = new CreateHouseholdUseCase(households, clock, views);

        HouseholdView result = useCase.execute(new AccountId(1L), "Casa Azul");

        assertThat(result.name()).isEqualTo("Casa Azul");
        assertThat(result.members()).singleElement().satisfies(member -> {
            assertThat(member.accountId()).isEqualTo(1L);
            assertThat(member.owner()).isTrue();
            assertThat(member.role()).isEqualTo(HouseholdRole.ADMIN);
            assertThat(member.email()).isEqualTo("account-1@example.com");
        });
    }

    @Test
    void shouldListOnlyActiveHouseholdsForTheAuthenticatedAccount() {
        Household household = createHousehold();
        var useCase = new ListAccountHouseholdsUseCase(households);

        assertThat(useCase.execute(new AccountId(1L))).singleElement().satisfies(summary -> {
            assertThat(summary.id()).isEqualTo(household.id().value());
            assertThat(summary.currentRole()).isEqualTo(HouseholdRole.ADMIN);
            assertThat(summary.owner()).isTrue();
        });
        assertThat(useCase.execute(new AccountId(99L))).isEmpty();
    }

    @Test
    void shouldListPendingInvitationsForAnAdministrator() {
        Household household = createHousehold();
        HouseholdInvitation invitation = HouseholdInvitation.create(new InvitationId(java.util.UUID.randomUUID()),
                household.id(), InvitationEmail.of("person@example.com"), HouseholdRole.MEMBER,
                household.ownerMemberId(), new InvitationTokenHash("d".repeat(64)), NOW,
                ncasa.household.domain.InvitationExpiry.after(NOW, Duration.ofDays(7)));
        invitations.save(invitation);

        var result = new GetHouseholdPendingInvitationsUseCase(households, invitations, clock)
                .execute(household.id(), new AccountId(1L));

        assertThat(result).singleElement().satisfies(view -> {
            assertThat(view.email()).isEqualTo("person@example.com");
            assertThat(view.status()).isEqualTo(InvitationStatus.PENDING);
        });
    }

    @Test
    void shouldReplacePendingInvitationAndReportDelivery() {
        Household household = createHousehold();
        var tokens = new SequenceTokens("first-token", "second-token");
        InvitationTokenHasher hasher = raw -> new InvitationTokenHash(raw.equals("first-token") ? "a".repeat(64) : "b".repeat(64));
        InvitationDeliveryPort delivery = (invitation, rawToken) -> {};
        var useCase = new InviteHouseholdMemberUseCase(households, invitations, tokens, hasher,
                delivery, clock, Duration.ofDays(7));

        useCase.execute(household.id(), new AccountId(1L), "user@example.com", HouseholdRole.MEMBER);
        InvitationResult second = useCase.execute(household.id(), new AccountId(1L),
                "user@example.com", HouseholdRole.MEMBER);

        assertThat(second.deliverySucceeded()).isTrue();
        assertThat(invitations.values).hasSize(2);
        assertThat(invitations.values.getFirst().status()).isEqualTo(InvitationStatus.CANCELLED);
        assertThat(invitations.values.getLast().status()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    void shouldAcceptVisibleInvitationAndReactivateExistingMembership() {
        Household household = createHousehold();
        var acceptance = new InvitationAcceptanceService(households, invitations, clock);
        var acceptById = new AcceptInvitationByIdUseCase(invitations, acceptance, views);
        InvitationId invitationId = new InvitationId(java.util.UUID.randomUUID());
        HouseholdInvitation first = HouseholdInvitation.create(invitationId, household.id(),
                InvitationEmail.of("user@example.com"), HouseholdRole.MEMBER, household.ownerMemberId(),
                new InvitationTokenHash("a".repeat(64)), NOW,
                ncasa.household.domain.InvitationExpiry.after(NOW, Duration.ofDays(7)));
        invitations.save(first);

        acceptById.execute(invitationId, new AccountId(2L), "user@example.com");
        var memberId = household.activeMemberFor(new AccountId(2L)).id();
        household.leave(memberId, NOW.plusSeconds(1));

        HouseholdInvitation second = HouseholdInvitation.create(new InvitationId(java.util.UUID.randomUUID()),
                household.id(), InvitationEmail.of("user@example.com"), HouseholdRole.ADMIN,
                household.ownerMemberId(), new InvitationTokenHash("b".repeat(64)), NOW,
                ncasa.household.domain.InvitationExpiry.after(NOW, Duration.ofDays(7)));
        invitations.save(second);
        acceptById.execute(second.id(), new AccountId(2L), "user@example.com");

        assertThat(household.activeMemberFor(new AccountId(2L)).id()).isEqualTo(memberId);
        assertThat(household.activeMemberFor(new AccountId(2L)).role()).isEqualTo(HouseholdRole.ADMIN);
    }

    @Test
    void shouldHideInvitationWhenAuthenticatedEmailDoesNotMatch() {
        Household household = createHousehold();
        var acceptance = new InvitationAcceptanceService(households, invitations, clock);
        var acceptById = new AcceptInvitationByIdUseCase(invitations, acceptance, views);
        HouseholdInvitation invitation = HouseholdInvitation.create(new InvitationId(java.util.UUID.randomUUID()),
                household.id(), InvitationEmail.of("user@example.com"), HouseholdRole.MEMBER,
                household.ownerMemberId(), new InvitationTokenHash("c".repeat(64)), NOW,
                ncasa.household.domain.InvitationExpiry.after(NOW, Duration.ofDays(7)));
        invitations.save(invitation);

        assertThatThrownBy(() -> acceptById.execute(invitation.id(), new AccountId(2L), "other@example.com"))
                .isInstanceOf(InvitationNotFoundException.class);
    }

    private Household createHousehold() {
        new CreateHouseholdUseCase(households, clock, views).execute(new AccountId(1L), "Casa Azul");
        return households.values.values().iterator().next();
    }

    private static final class InMemoryHouseholds implements HouseholdRepository {
        private final Map<HouseholdId, Household> values = new LinkedHashMap<>();
        public Optional<Household> findById(HouseholdId id) { return Optional.ofNullable(values.get(id)); }
        public List<Household> findActiveByMemberAccountId(AccountId accountId) {
            return values.values().stream().filter(h -> {
                try { h.activeMemberFor(accountId); return h.status() == ncasa.household.domain.HouseholdStatus.ACTIVE; }
                catch (RuntimeException ignored) { return false; }
            }).toList();
        }
        public Household save(Household household) { values.put(household.id(), household); return household; }
    }

    private static final class InMemoryInvitations implements HouseholdInvitationRepository {
        private final List<HouseholdInvitation> values = new ArrayList<>();
        public Optional<HouseholdInvitation> findById(InvitationId id) { return values.stream().filter(i -> i.id().equals(id)).findFirst(); }
        public Optional<HouseholdInvitation> findByTokenHash(InvitationTokenHash hash) { return values.stream().filter(i -> i.tokenHash().equals(hash)).findFirst(); }
        public Optional<HouseholdInvitation> findPending(HouseholdId householdId, InvitationEmail email) {
            return values.stream().filter(i -> i.householdId().equals(householdId) && i.email().equals(email)
                    && i.status() == InvitationStatus.PENDING).findFirst();
        }
        public List<HouseholdInvitation> findPendingByEmail(InvitationEmail email) {
            return values.stream().filter(i -> i.email().equals(email) && i.status() == InvitationStatus.PENDING).toList();
        }
        public List<HouseholdInvitation> findPendingByHousehold(HouseholdId householdId) {
            return values.stream().filter(i -> i.householdId().equals(householdId)
                    && i.status() == InvitationStatus.PENDING).toList();
        }
        public HouseholdInvitation save(HouseholdInvitation invitation) {
            if (!values.contains(invitation)) values.add(invitation);
            return invitation;
        }
    }

    private static final class SequenceTokens implements InvitationTokenGenerator {
        private final List<String> tokens;
        private int index;
        SequenceTokens(String... tokens) { this.tokens = List.of(tokens); }
        public String generate() { return tokens.get(index++); }
    }
}
