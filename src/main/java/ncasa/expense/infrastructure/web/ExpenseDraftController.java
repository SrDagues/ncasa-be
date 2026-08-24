package ncasa.expense.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import ncasa.expense.application.ExpenseDraftView;
import ncasa.expense.application.create.*;
import ncasa.expense.application.draft.*;
import ncasa.expense.domain.*;
import ncasa.identityaccess.infrastructure.security.IdentityUserDetails;
import org.springframework.http.HttpStatus;
import org.slf4j.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/households/{householdId}/expense-drafts")
@Transactional
public class ExpenseDraftController {
    private static final Logger LOG=LoggerFactory.getLogger(ExpenseDraftController.class);
    private final CreateExpenseDraftUseCase create;private final GetExpenseDraftUseCase get;private final ListExpenseDraftsUseCase list;
    private final UpdateExpenseDraftUseCase update;private final DiscardExpenseDraftUseCase discard;private final ConfirmExpenseDraftUseCase confirm;
    public ExpenseDraftController(CreateExpenseDraftUseCase create,GetExpenseDraftUseCase get,ListExpenseDraftsUseCase list,
            UpdateExpenseDraftUseCase update,DiscardExpenseDraftUseCase discard,ConfirmExpenseDraftUseCase confirm){
        this.create=create;this.get=get;this.list=list;this.update=update;this.discard=discard;this.confirm=confirm;
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    DraftResponse create(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId){var result=create.execute(user.id(),householdId);log("expense_draft_created",householdId,result.id());return DraftResponse.from(result);}
    @GetMapping("/{draftId}")@Transactional(readOnly=true)
    DraftResponse get(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,@PathVariable UUID draftId){return DraftResponse.from(get.execute(user.id(),householdId,draftId));}
    @GetMapping @Transactional(readOnly=true)
    List<DraftResponse> list(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,
            @RequestParam(defaultValue="OPEN") ExpenseDraftStatus status){return list.execute(user.id(),householdId,status).stream().map(DraftResponse::from).toList();}
    @PutMapping("/{draftId}")
    DraftResponse update(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,@PathVariable UUID draftId,
            @Valid @RequestBody UpdateDraftRequest request){var result=update.execute(new UpdateExpenseDraftCommand(user.id(),householdId,draftId,
                    request.description(),request.amount(),request.currency(),request.expenseDate(),request.payerMemberId(),request.categoryId(),
                    request.split()==null?null:request.split().toCommand(),request.version()));log("expense_draft_updated",householdId,draftId);return DraftResponse.from(result);}
    @PostMapping("/{draftId}/discard")
    DraftResponse discard(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,@PathVariable UUID draftId,
            @Valid @RequestBody VersionRequest request){var result=discard.execute(user.id(),householdId,draftId,request.version());log("expense_draft_discarded",householdId,draftId);return DraftResponse.from(result);}
    @PostMapping("/{draftId}/confirm")
    ExpenseResponse confirm(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,@PathVariable UUID draftId,
            @Valid @RequestBody VersionRequest request){var result=confirm.execute(user.id(),householdId,draftId,request.version());log("expense_draft_confirmed",householdId,draftId);return ExpenseResponse.from(result);}

    record UpdateDraftRequest(@Size(max=240)String description,@DecimalMin(value="0.0",inclusive=false)BigDecimal amount,
            @Size(min=3,max=3)String currency,LocalDate expenseDate,UUID payerMemberId,UUID categoryId,@Valid SplitRequest split,@PositiveOrZero long version){}
    record SplitRequest(@NotNull ExpenseSplitType type,List<@NotNull UUID> memberIds,List<@Valid AllocationRequest> allocations){
        ExpenseSplitCommand toCommand(){
            if(type==ExpenseSplitType.EQUAL){if(allocations!=null&&!allocations.isEmpty())throw new IllegalArgumentException("EQUAL split only accepts memberIds");return new EqualSplitCommand(memberIds);}
            if(memberIds!=null&&!memberIds.isEmpty())throw new IllegalArgumentException(type+" split only accepts allocations");
            if(type==ExpenseSplitType.EXACT)return new ExactSplitCommand(allocations==null?List.of():allocations.stream().map(a->{if(a.amount()==null)throw new IllegalArgumentException("EXACT allocation amount is required");if(a.percentage()!=null)throw new IllegalArgumentException("EXACT allocation cannot contain percentage");return new ExactAllocationCommand(a.memberId(),a.amount());}).toList());
            return new PercentageSplitCommand(allocations==null?List.of():allocations.stream().map(a->{if(a.percentage()==null)throw new IllegalArgumentException("PERCENTAGE allocation percentage is required");if(a.amount()!=null)throw new IllegalArgumentException("PERCENTAGE allocation cannot contain amount");return new PercentageAllocationCommand(a.memberId(),a.percentage());}).toList());
        }
    }
    record AllocationRequest(@NotNull UUID memberId,BigDecimal amount,BigDecimal percentage){}
    record VersionRequest(@PositiveOrZero long version){}
    private static void log(String action,UUID householdId,UUID draftId){LOG.atInfo().addKeyValue("event.action",action).addKeyValue("household.id",householdId).addKeyValue("expense.draft.id",draftId).log(action);}
    record DraftAllocationResponse(UUID memberId,String amount,String percentage){}
    record DraftResponse(UUID id,UUID householdId,UUID createdByMemberId,String description,UUID payerMemberId,
            String amount,String currency,LocalDate expenseDate,UUID categoryId,ExpenseSplitType splitType,
            List<DraftAllocationResponse> allocations,ExpenseDraftStatus status,UUID confirmedExpenseId,
            Instant createdAt,Instant updatedAt,Instant confirmedAt,Instant discardedAt,long version){
        static DraftResponse from(ExpenseDraftView v){return new DraftResponse(v.id(),v.householdId(),v.createdByMemberId(),v.description(),v.payerMemberId(),
                v.amount()==null?null:v.amount().toPlainString(),v.currency(),v.expenseDate(),v.categoryId(),v.splitType(),v.allocations().stream()
                .map(a->new DraftAllocationResponse(a.memberId(),a.amount()==null?null:a.amount().toPlainString(),a.percentage()==null?null:a.percentage().toPlainString())).toList(),
                v.status(),v.confirmedExpenseId(),v.createdAt(),v.updatedAt(),v.confirmedAt(),v.discardedAt(),v.version());}
    }
}
