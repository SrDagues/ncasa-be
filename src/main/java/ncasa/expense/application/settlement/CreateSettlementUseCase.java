package ncasa.expense.application.settlement;
import java.time.*; import ncasa.expense.application.*; import ncasa.expense.application.debt.*; import ncasa.expense.application.port.out.*; import ncasa.expense.domain.*;
public final class CreateSettlementUseCase {
 private final SettlementRepository repository; private final HouseholdExpenseAccessPort access; private final GetDebtSummaryUseCase debt; private final Clock clock;
 public CreateSettlementUseCase(SettlementRepository r,HouseholdExpenseAccessPort a,GetDebtSummaryUseCase d,Clock c){repository=r;access=a;debt=d;clock=c;}
 public CreateSettlementResult execute(CreateSettlementCommand c){
  var household=new HouseholdRef(c.householdId());var context=access.getContext(household,c.actorAccountId());var from=new MemberRef(c.fromMemberId());var to=new MemberRef(c.toMemberId());var money=new Money(c.amount(),c.currency());
  var existing=repository.findByIdempotency(household,context.actorMemberId(),c.idempotencyKey());
  if(existing.isPresent()){var s=existing.get();if(!matches(s,from,to,money,c.settlementDate(),c.note()))throw new SettlementConflictException("Idempotency key was used with a different payload");return new CreateSettlementResult(SettlementView.from(s),true);}
  context.requireMember(from);context.requireMember(to);if(!context.administrator()&&!context.actorMemberId().equals(from)&&!context.actorMemberId().equals(to))throw new ExpenseAccessDeniedException("Actor must participate in the settlement or be an administrator");
  var today=LocalDate.now(clock);if(c.settlementDate().isAfter(today))throw new ExpenseRuleViolationException("Settlement date cannot be in the future");
  var summary=debt.execute(c.actorAccountId(),c.householdId());var currency=summary.currencies().stream().filter(x->x.currency().equals(money.currency())).findFirst().orElseThrow(()->new SettlementConflictException("Currency has no pending debt"));
  var fromPosition=currency.members().stream().filter(x->x.memberId().equals(c.fromMemberId())).findFirst().orElseThrow();var toPosition=currency.members().stream().filter(x->x.memberId().equals(c.toMemberId())).findFirst().orElseThrow();
  if(fromPosition.net().signum()>=0||toPosition.net().signum()<=0)throw new SettlementConflictException("Settlement does not reduce pending debt");
  if(money.amount().compareTo(fromPosition.net().abs())>0||money.amount().compareTo(toPosition.net())>0)throw new SettlementConflictException("Settlement amount exceeds pending debt");
  var now=clock.instant();var saved=repository.save(Settlement.confirmed(SettlementId.newId(),household,context.actorMemberId(),from,to,money,c.settlementDate(),c.note(),now),c.idempotencyKey());return new CreateSettlementResult(SettlementView.from(saved),false);
 }
 private boolean matches(Settlement s,MemberRef from,MemberRef to,Money money,LocalDate date,String note){var normalized=note==null||note.isBlank()?null:note.trim();return s.fromMemberId().equals(from)&&s.toMemberId().equals(to)&&s.money().equals(money)&&s.settlementDate().equals(date)&&java.util.Objects.equals(s.note(),normalized);}
}
