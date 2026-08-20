package ncasa.expense.domain;
import static org.assertj.core.api.Assertions.*;import java.util.*;import org.junit.jupiter.api.Test;
class DebtCalculatorTest {
 private static MemberRef member(String suffix){return new MemberRef(UUID.fromString("00000000-0000-0000-0000-0000000000"+suffix));}
 @Test void createsDeterministicGreedySuggestions(){var result=new DebtCalculator().calculate(List.of(new MemberFinancialPosition(member("01"),Money.of("-7.50","EUR")),new MemberFinancialPosition(member("02"),Money.of("-2.50","EUR")),new MemberFinancialPosition(member("03"),Money.of("10.00","EUR"))));assertThat(result).containsExactly(new SuggestedSettlement(member("01"),member("03"),Money.of("7.50","EUR")),new SuggestedSettlement(member("02"),member("03"),Money.of("2.50","EUR")));}
 @Test void rejectsUnbalancedPositions(){assertThatThrownBy(()->new DebtCalculator().calculate(List.of(new MemberFinancialPosition(member("01"),Money.of("1.00","EUR"))))).isInstanceOf(ExpenseRuleViolationException.class);}
 @Test void returnsNothingWhenSettled(){assertThat(new DebtCalculator().calculate(List.of(new MemberFinancialPosition(member("01"),Money.zero("EUR"))))).isEmpty();}
}
