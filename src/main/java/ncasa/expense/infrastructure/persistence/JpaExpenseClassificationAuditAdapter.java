package ncasa.expense.infrastructure.persistence;

import java.util.List;
import ncasa.expense.application.port.out.ExpenseClassificationAuditRepository;
import ncasa.expense.domain.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaExpenseClassificationAuditAdapter implements ExpenseClassificationAuditRepository{
    private final SpringDataExpenseClassificationRepository repository;
    public JpaExpenseClassificationAuditAdapter(SpringDataExpenseClassificationRepository repository){this.repository=repository;}
    @Override public ExpenseClassificationChange save(ExpenseClassificationChange c){return toDomain(repository.saveAndFlush(toEntity(c)));}
    @Override public List<ExpenseClassificationChange> findByExpense(ExpenseId expenseId,HouseholdRef household){return repository.findByExpenseIdAndHouseholdIdOrderByChangedAtAscIdAsc(expenseId.value(),household.value()).stream().map(this::toDomain).toList();}
    private JpaExpenseClassificationChangeEntity toEntity(ExpenseClassificationChange c){return new JpaExpenseClassificationChangeEntity(c.id(),c.expenseId().value(),c.householdId().value(),c.changedByMemberId().value(),
            c.previousCategoryId()==null?null:c.previousCategoryId().value(),c.newCategoryId()==null?null:c.newCategoryId().value(),c.reason(),c.changedAt());}
    private ExpenseClassificationChange toDomain(JpaExpenseClassificationChangeEntity e){return new ExpenseClassificationChange(e.id(),new ExpenseId(e.expenseId()),new HouseholdRef(e.householdId()),new MemberRef(e.changedByMemberId()),
            e.previousCategoryId()==null?null:new ExpenseCategoryId(e.previousCategoryId()),e.newCategoryId()==null?null:new ExpenseCategoryId(e.newCategoryId()),e.reason(),e.changedAt());}
}
