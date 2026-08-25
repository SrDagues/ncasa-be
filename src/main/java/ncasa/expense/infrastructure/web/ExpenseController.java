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
import ncasa.expense.application.classification.*;
import ncasa.expense.application.ExpenseClassificationView;
import ncasa.expense.domain.ExpenseSplitType;
import ncasa.expense.domain.ExpenseStatus;
import ncasa.expense.domain.ExpenseSource;
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
    private final ReclassifyExpenseUseCase reclassify;
    private final ListExpenseClassificationHistoryUseCase classificationHistory;

    public ExpenseController(CreateExpenseUseCase create, GetExpenseUseCase get,
            ListExpensesUseCase list, VoidExpenseUseCase voidExpense,ReclassifyExpenseUseCase reclassify,
            ListExpenseClassificationHistoryUseCase classificationHistory) {
        this.create = create; this.get = get; this.list = list; this.voidExpense = voidExpense;
        this.reclassify=reclassify;this.classificationHistory=classificationHistory;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ExpenseResponse create(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID householdId,
            @Valid @RequestBody CreateExpenseRequest request) {
        var result = create.execute(new CreateExpenseCommand(user.id(), householdId, request.description(),
                request.amount(), request.currency(), request.expenseDate(), request.payerMemberId(),
                request.categoryId(), request.split().toCommand()));
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
            @RequestParam(required=false) UUID categoryId,@RequestParam(defaultValue="false")boolean uncategorized,
            @RequestParam(required=false)ExpenseSplitType splitType,
            @RequestParam(required=false)ExpenseSource source,@RequestParam(required=false)UUID planId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ExpensePageResponse.from(list.execute(new ListExpensesQuery(user.id(), householdId,
                from, to, status, payerMemberId, participantMemberId,categoryId,uncategorized,splitType,source,planId,page, size)));
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

    @PostMapping("/{expenseId}/reclassify")
    ExpenseResponse reclassify(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,
            @PathVariable UUID expenseId,@Valid @RequestBody ReclassifyRequest request){
        var result=reclassify.execute(user.id(),householdId,expenseId,request.categoryId(),request.reason());
        LOGGER.atInfo().addKeyValue("event.action","expense_reclassified").addKeyValue("household.id",householdId)
                .addKeyValue("expense.id",expenseId).addKeyValue("expense.category.id",request.categoryId()).log("expense_reclassified");
        return ExpenseResponse.from(result);
    }

    @GetMapping("/{expenseId}/classification-history")
    @Transactional(readOnly=true)
    List<ClassificationResponse> classificationHistory(@AuthenticationPrincipal IdentityUserDetails user,
            @PathVariable UUID householdId,@PathVariable UUID expenseId){
        return classificationHistory.execute(user.id(),householdId,expenseId).stream().map(ClassificationResponse::from).toList();
    }

    record CreateExpenseRequest(@NotBlank @Size(max = 240) String description,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) String currency, @NotNull LocalDate expenseDate,
            @NotNull UUID payerMemberId, UUID categoryId, @NotNull @Valid SplitRequest split) {}

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
                        .map(item -> {if(item.amount()==null)throw new IllegalArgumentException("EXACT allocation amount is required");
                            if(item.percentage()!=null)throw new IllegalArgumentException("EXACT allocation cannot contain percentage");
                            return new ExactAllocationCommand(item.memberId(), item.amount());}).toList());
            }
            if (type == ExpenseSplitType.PERCENTAGE) {
                if (memberIds != null && !memberIds.isEmpty()) {
                    throw new IllegalArgumentException("PERCENTAGE split cannot contain equal-split member ids");
                }
                return new PercentageSplitCommand(allocations == null ? List.of() : allocations.stream()
                        .map(item -> {if(item.percentage()==null)throw new IllegalArgumentException("PERCENTAGE allocation percentage is required");
                            if(item.amount()!=null)throw new IllegalArgumentException("PERCENTAGE allocation cannot contain amount");
                            return new PercentageAllocationCommand(item.memberId(), item.percentage());}).toList());
            }
            throw new IllegalArgumentException("Unsupported split type");
        }
    }

    record AllocationRequest(@NotNull UUID memberId,
            @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @DecimalMin(value = "0.0", inclusive = false) BigDecimal percentage) {}
    record VoidExpenseRequest(@NotBlank @Size(max = 500) String reason) {}
    record ReclassifyRequest(UUID categoryId,@Size(max=500)String reason){}
    record ClassificationResponse(UUID id,UUID expenseId,UUID changedByMemberId,UUID previousCategoryId,
            UUID newCategoryId,String reason,java.time.Instant changedAt){
        static ClassificationResponse from(ExpenseClassificationView v){return new ClassificationResponse(v.id(),v.expenseId(),v.changedByMemberId(),v.previousCategoryId(),v.newCategoryId(),v.reason(),v.changedAt());}
    }
}
