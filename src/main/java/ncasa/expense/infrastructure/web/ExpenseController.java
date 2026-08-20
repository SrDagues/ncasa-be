package ncasa.expense.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import ncasa.expense.application.create.*;
import ncasa.expense.application.get.GetExpenseUseCase;
import ncasa.expense.application.list.ListExpensesQuery;
import ncasa.expense.application.list.ListExpensesUseCase;
import ncasa.expense.application.voidexpense.VoidExpenseUseCase;
import ncasa.expense.domain.ExpenseSplitType;
import ncasa.expense.domain.ExpenseStatus;
import ncasa.identityaccess.infrastructure.security.IdentityUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/households/{householdId}/expenses")
@Transactional
public class ExpenseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpenseController.class);
    private final CreateExpenseUseCase create;
    private final GetExpenseUseCase get;
    private final ListExpensesUseCase list;
    private final VoidExpenseUseCase voidExpense;

    public ExpenseController(CreateExpenseUseCase create, GetExpenseUseCase get,
            ListExpensesUseCase list, VoidExpenseUseCase voidExpense) {
        this.create = create; this.get = get; this.list = list; this.voidExpense = voidExpense;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ExpenseResponse create(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @Valid @RequestBody CreateExpenseRequest request) {
        var result = create.execute(new CreateExpenseCommand(user.id(), householdId, request.description(),
                request.amount(), request.currency(), request.expenseDate(), request.payerMemberId(),
                request.split().toCommand()));
        LOGGER.atInfo().addKeyValue("event.action", "expense_created")
                .addKeyValue("expense.id", result.id()).addKeyValue("household.id", householdId)
                .log("expense_created");
        return ExpenseResponse.from(result);
    }

    @GetMapping("/{expenseId}")
    @Transactional(readOnly = true)
    ExpenseResponse get(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID expenseId) {
        return ExpenseResponse.from(get.execute(user.id(), householdId, expenseId));
    }

    @GetMapping
    @Transactional(readOnly = true)
    ExpensePageResponse list(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "CONFIRMED") ExpenseStatus status,
            @RequestParam(required = false) UUID payerMemberId,
            @RequestParam(required = false) UUID participantMemberId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ExpensePageResponse.from(list.execute(new ListExpensesQuery(user.id(), householdId,
                from, to, status, payerMemberId, participantMemberId, page, size)));
    }

    @PostMapping("/{expenseId}/void")
    ExpenseResponse voidExpense(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @PathVariable UUID expenseId, @Valid @RequestBody VoidExpenseRequest request) {
        var result = voidExpense.execute(user.id(), householdId, expenseId, request.reason());
        LOGGER.atInfo().addKeyValue("event.action", "expense_voided")
                .addKeyValue("expense.id", expenseId).addKeyValue("household.id", householdId)
                .log("expense_voided");
        return ExpenseResponse.from(result);
    }

    record CreateExpenseRequest(@NotBlank @Size(max = 240) String description,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) String currency, @NotNull LocalDate expenseDate,
            @NotNull UUID payerMemberId, @NotNull @Valid SplitRequest split) {}

    record SplitRequest(@NotNull ExpenseSplitType type, List<@NotNull UUID> memberIds,
            List<@Valid AllocationRequest> allocations) {
        ExpenseSplitCommand toCommand() {
            if (type == ExpenseSplitType.EQUAL) {
                if (allocations != null && !allocations.isEmpty()) {
                    throw new IllegalArgumentException("EQUAL split cannot contain exact allocations");
                }
                return new EqualSplitCommand(memberIds);
            }
            if (type == ExpenseSplitType.EXACT) {
                if (memberIds != null && !memberIds.isEmpty()) {
                    throw new IllegalArgumentException("EXACT split cannot contain equal-split member ids");
                }
                return new ExactSplitCommand(allocations == null ? List.of() : allocations.stream()
                        .map(item -> new ExactAllocationCommand(item.memberId(), item.amount())).toList());
            }
            throw new IllegalArgumentException("Unsupported split type");
        }
    }

    record AllocationRequest(@NotNull UUID memberId,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount) {}
    record VoidExpenseRequest(@NotBlank @Size(max = 500) String reason) {}
}
