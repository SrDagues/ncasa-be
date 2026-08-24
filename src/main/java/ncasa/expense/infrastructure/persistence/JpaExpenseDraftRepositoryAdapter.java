package ncasa.expense.infrastructure.persistence;

import java.nio.charset.StandardCharsets;
import java.util.*;
import ncasa.expense.application.port.out.ExpenseDraftRepository;
import ncasa.expense.domain.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaExpenseDraftRepositoryAdapter implements ExpenseDraftRepository{
    private final SpringDataExpenseDraftRepository repository;
    public JpaExpenseDraftRepositoryAdapter(SpringDataExpenseDraftRepository repository){this.repository=repository;}
    @Override public ExpenseDraft save(ExpenseDraft draft){return toDomain(repository.saveAndFlush(toEntity(draft)));}
    @Override public Optional<ExpenseDraft> findByIdAndHousehold(ExpenseDraftId id,HouseholdRef household){return repository.findComplete(id.value(),household.value()).map(this::toDomain);}
    @Override public List<ExpenseDraft> findByCreator(HouseholdRef household,MemberRef creator,ExpenseDraftStatus status){return repository.findByCreator(household.value(),creator.value(),status==null?null:status.name()).stream().map(this::toDomain).toList();}
    private JpaExpenseDraftEntity toEntity(ExpenseDraft d){var c=d.content();var entity=new JpaExpenseDraftEntity(d.id().value(),d.householdId().value(),d.createdByMemberId().value(),
            c.description()==null?null:c.description().value(),c.payerMemberId()==null?null:c.payerMemberId().value(),c.total()==null?null:c.total().amount(),c.total()==null?null:c.total().currency(),
            c.expenseDate(),c.categoryId()==null?null:c.categoryId().value(),c.split()==null?null:c.split().type().name(),d.status().name(),d.confirmedExpenseId()==null?null:d.confirmedExpenseId().value(),
            d.createdAt(),d.updatedAt(),d.confirmedAt(),d.discardedAt(),d.version());
        allocations(d.id(),c.split()).forEach(entity::addAllocation);return entity;}
    private List<JpaExpenseDraftAllocationEntity> allocations(ExpenseDraftId draftId,DraftSplit split){if(split==null)return List.of();
        if(split instanceof EqualDraftSplit e)return e.members().stream().map(m->allocation(draftId,m,null,null)).toList();
        if(split instanceof ExactDraftSplit e)return e.allocations().stream().map(a->allocation(draftId,a.memberId(),a.amount().amount(),null)).toList();
        return ((PercentageDraftSplit)split).allocations().stream().map(a->allocation(draftId,a.memberId(),null,a.percentage().value())).toList();}
    private JpaExpenseDraftAllocationEntity allocation(ExpenseDraftId draftId,MemberRef member,java.math.BigDecimal amount,java.math.BigDecimal percentage){return new JpaExpenseDraftAllocationEntity(
            UUID.nameUUIDFromBytes((draftId.value()+":"+member.value()).getBytes(StandardCharsets.UTF_8)),member.value(),amount,percentage);}
    private ExpenseDraft toDomain(JpaExpenseDraftEntity e){Money total=e.amount()==null?null:new Money(e.amount(),e.currency());DraftSplit split=null;
        if(e.splitType()!=null){var type=ExpenseSplitType.valueOf(e.splitType());if(type==ExpenseSplitType.EQUAL)split=new EqualDraftSplit(e.allocations().stream().map(a->new MemberRef(a.memberId())).toList());
            else if(type==ExpenseSplitType.EXACT)split=new ExactDraftSplit(e.allocations().stream().map(a->new ExpenseAllocation(new MemberRef(a.memberId()),new Money(a.amount(),e.currency()))).toList());
            else split=new PercentageDraftSplit(e.allocations().stream().map(a->new PercentageAllocation(new MemberRef(a.memberId()),Percentage.of(a.percentage()))).toList());}
        var content=new ExpenseDraftContent(e.description()==null?null:new ExpenseDescription(e.description()),e.payerMemberId()==null?null:new MemberRef(e.payerMemberId()),total,e.expenseDate(),e.categoryId()==null?null:new ExpenseCategoryId(e.categoryId()),split);
        return ExpenseDraft.rehydrate(new ExpenseDraftId(e.id()),new HouseholdRef(e.householdId()),new MemberRef(e.createdByMemberId()),content,ExpenseDraftStatus.valueOf(e.status()),
                e.confirmedExpenseId()==null?null:new ExpenseId(e.confirmedExpenseId()),e.createdAt(),e.updatedAt(),e.confirmedAt(),e.discardedAt(),e.version());}
}
