package ncasa.expense.infrastructure.persistence;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import ncasa.expense.application.port.out.ExpensePageSlice;
import ncasa.expense.application.port.out.ExpenseRepository;
import ncasa.expense.domain.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class JpaExpenseRepositoryAdapter implements ExpenseRepository {
    private final SpringDataExpenseRepository repository;
    public JpaExpenseRepositoryAdapter(SpringDataExpenseRepository repository) { this.repository = repository; }

    @Override
    public Expense save(Expense expense) {
        return toDomain(repository.saveAndFlush(toEntity(expense)));
    }

    @Override
    public Optional<Expense> findByIdAndHousehold(ExpenseId id, HouseholdRef householdId) {
        return repository.findByIdAndHouseholdId(id.value(), householdId.value()).map(this::toDomain);
    }

    @Override
    public ExpensePageSlice findPage(HouseholdRef householdId, LocalDate from, LocalDate to,
            ExpenseStatus status, int page, int size) {
        var ids = repository.findPageIds(householdId.value(), from, to,
                status == null ? null : status.name(), PageRequest.of(page, size));
        if (ids.isEmpty()) return new ExpensePageSlice(java.util.List.of(), ids.getTotalElements());
        Map<UUID, JpaExpenseEntity> entities = repository.findByIdIn(ids.getContent()).stream()
                .collect(Collectors.toMap(JpaExpenseEntity::id, Function.identity()));
        var ordered = ids.getContent().stream().map(entities::get).map(this::toDomain).toList();
        return new ExpensePageSlice(ordered, ids.getTotalElements());
    }

    private JpaExpenseEntity toEntity(Expense expense) {
        var entity = new JpaExpenseEntity(expense.id().value(), expense.householdId().value(),
                expense.createdByMemberId().value(), expense.payerMemberId().value(), expense.description().value(),
                expense.total().amount(), expense.total().currency(), expense.expenseDate(),
                expense.split().type().name(), expense.status().name(), expense.source().name(),
                expense.voidReason() == null ? null : expense.voidReason().value(), expense.createdAt(),
                expense.updatedAt(), expense.voidedAt(), expense.version());
        expense.split().allocations().stream()
                .sorted(Comparator.comparing(allocation -> allocation.memberId().value()))
                .forEach(allocation -> entity.addAllocation(new JpaExpenseAllocationEntity(
                        allocationId(expense.id(), allocation.memberId()), allocation.memberId().value(),
                        allocation.amount().amount())));
        return entity;
    }

    private Expense toDomain(JpaExpenseEntity entity) {
        Money total = new Money(entity.amount(), entity.currency());
        var allocations = entity.allocations().stream()
                .map(allocation -> new ExpenseAllocation(new MemberRef(allocation.memberId()),
                        new Money(allocation.amount(), entity.currency())))
                .sorted(Comparator.comparing(allocation -> allocation.memberId().value())).toList();
        ExpenseSplit split = ExpenseSplit.rehydrate(ExpenseSplitType.valueOf(entity.splitType()), total, allocations);
        return Expense.rehydrate(new ExpenseId(entity.id()), new HouseholdRef(entity.householdId()),
                new MemberRef(entity.createdByMemberId()), new MemberRef(entity.payerMemberId()), total,
                new ExpenseDescription(entity.description()), entity.expenseDate(), split,
                ExpenseStatus.valueOf(entity.status()), ExpenseSource.valueOf(entity.source()), entity.createdAt(),
                entity.updatedAt(), entity.voidReason() == null ? null : new VoidReason(entity.voidReason()),
                entity.voidedAt(), entity.version());
    }

    private UUID allocationId(ExpenseId expenseId, MemberRef memberId) {
        return UUID.nameUUIDFromBytes((expenseId.value() + ":" + memberId.value()).getBytes(StandardCharsets.UTF_8));
    }
}
