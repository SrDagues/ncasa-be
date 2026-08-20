package ncasa.household.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import ncasa.household.application.port.out.HouseholdRepository;
import ncasa.household.domain.*;
import ncasa.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class JpaHouseholdRepositoryAdapterIT extends PostgresIntegrationTest {
    @Autowired HouseholdRepository households;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUpAccount() {
        jdbc.update("DELETE FROM household_invitations");
        jdbc.update("DELETE FROM household_members");
        jdbc.update("DELETE FROM households");
        jdbc.update("DELETE FROM auth_identities");
        jdbc.update("DELETE FROM user_roles");
        jdbc.update("DELETE FROM refresh_tokens");
        jdbc.update("DELETE FROM users");
        jdbc.update("INSERT INTO users(id,email,enabled,created_at,updated_at) VALUES (?,?,?,?,?)",
                1L, "owner@example.com", true, Instant.now(), Instant.now());
    }

    @Test
    void shouldPersistAndRehydrateAggregateWithMembers() {
        Instant now = Instant.parse("2026-08-20T10:00:00Z");
        Household original = Household.create(new HouseholdId(UUID.randomUUID()), HouseholdName.of("Casa Azul"),
                new MemberId(UUID.randomUUID()), new AccountId(1L), now);

        Household saved = households.save(original);
        Household reloaded = households.findById(saved.id()).orElseThrow();

        assertThat(reloaded.name().value()).isEqualTo("Casa Azul");
        assertThat(reloaded.members()).singleElement().satisfies(member -> assertThat(member.accountId().value()).isEqualTo(1L));
    }

    @Test
    void shouldRejectConcurrentAggregateUpdate() {
        Instant now = Instant.parse("2026-08-20T10:00:00Z");
        Household created = households.save(Household.create(new HouseholdId(UUID.randomUUID()),
                HouseholdName.of("Casa"), new MemberId(UUID.randomUUID()), new AccountId(1L), now));
        Household first = households.findById(created.id()).orElseThrow();
        Household second = households.findById(created.id()).orElseThrow();
        first.rename(first.ownerMemberId(), HouseholdName.of("Primera"), now.plusSeconds(1));
        second.rename(second.ownerMemberId(), HouseholdName.of("Segunda"), now.plusSeconds(2));

        households.save(first);
        assertThatThrownBy(() -> households.save(second))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
