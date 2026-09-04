package ncasa.expense.domain;
import static org.assertj.core.api.Assertions.*;import java.time.*;import java.util.UUID;import org.junit.jupiter.api.Test;
class SettlementTest {
 private final HouseholdRef household=new HouseholdRef(UUID.randomUUID());private final MemberRef from=new MemberRef(UUID.randomUUID());private final MemberRef to=new MemberRef(UUID.randomUUID());private final Instant now=Instant.parse("2026-08-20T10:00:00Z");
 @Test void createsAndVoidsSettlement(){var s=Settlement.confirmed(SettlementId.newId(),household,from,from,to,Money.of("12.34","EUR"),LocalDate.parse("2026-08-20")," Bizum ",now);assertThat(s.note()).isEqualTo("Bizum");s.voidSettlement(" error ",now.plusSeconds(1));assertThat(s.status()).isEqualTo(SettlementStatus.VOIDED);assertThat(s.voidReason()).isEqualTo("error");}
 @Test void rejectsInvalidTransfer(){assertThatThrownBy(()->Settlement.confirmed(SettlementId.newId(),household,from,from,from,Money.of("1","EUR"),LocalDate.now(),null,now)).isInstanceOf(ExpenseRuleViolationException.class);}
 @Test void cannotVoidTwice(){var s=Settlement.confirmed(SettlementId.newId(),household,from,from,to,Money.of("1","EUR"),LocalDate.now(),null,now);s.voidSettlement("x",now);assertThatThrownBy(()->s.voidSettlement("again",now)).isInstanceOf(SettlementStateException.class);}
}
