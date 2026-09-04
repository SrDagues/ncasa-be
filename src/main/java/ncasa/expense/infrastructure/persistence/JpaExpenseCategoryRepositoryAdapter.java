package ncasa.expense.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import ncasa.expense.application.port.out.ExpenseCategoryRepository;
import ncasa.expense.domain.*;
import org.springframework.stereotype.Repository;

@Repository
public class JpaExpenseCategoryRepositoryAdapter implements ExpenseCategoryRepository {
    private final SpringDataExpenseCategoryRepository repository;
    public JpaExpenseCategoryRepositoryAdapter(SpringDataExpenseCategoryRepository repository){this.repository=repository;}
    @Override public ExpenseCategory save(ExpenseCategory category){return toDomain(repository.saveAndFlush(toEntity(category)));}
    @Override public Optional<ExpenseCategory> findByIdAndHousehold(ExpenseCategoryId id,HouseholdRef household){
        return repository.findByIdAndHouseholdId(id.value(),household.value()).map(this::toDomain);
    }
    @Override public boolean existsActiveByName(HouseholdRef household,CategoryName name,ExpenseCategoryId excluded){
        return repository.existsActiveByName(household.value(),name.value(),excluded==null?null:excluded.value());
    }
    @Override public List<ExpenseCategory> findAll(HouseholdRef household,boolean includeArchived){
        var values=includeArchived?repository.findByHouseholdIdOrderByNameAscIdAsc(household.value()):
                repository.findByHouseholdIdAndStatusOrderByNameAscIdAsc(household.value(),"ACTIVE");
        return values.stream().map(this::toDomain).toList();
    }
    private JpaExpenseCategoryEntity toEntity(ExpenseCategory c){return new JpaExpenseCategoryEntity(c.id().value(),c.householdId().value(),
            c.createdByMemberId().value(),c.name().value(),c.status().name(),c.createdAt(),c.updatedAt(),c.archivedAt(),c.version());}
    private ExpenseCategory toDomain(JpaExpenseCategoryEntity c){return ExpenseCategory.rehydrate(new ExpenseCategoryId(c.id()),
            new HouseholdRef(c.householdId()),new MemberRef(c.createdByMemberId()),new CategoryName(c.name()),
            ExpenseCategoryStatus.valueOf(c.status()),c.createdAt(),c.updatedAt(),c.archivedAt(),c.version());}
}
