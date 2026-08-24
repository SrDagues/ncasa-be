package ncasa.expense.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import ncasa.expense.application.ExpenseCategoryView;
import ncasa.expense.application.category.*;
import ncasa.expense.domain.ExpenseCategoryStatus;
import ncasa.identityaccess.infrastructure.security.IdentityUserDetails;
import org.springframework.http.HttpStatus;
import org.slf4j.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/households/{householdId}/expense-categories")
@Transactional
public class ExpenseCategoryController {
    private static final Logger LOG=LoggerFactory.getLogger(ExpenseCategoryController.class);
    private final CreateExpenseCategoryUseCase create; private final RenameExpenseCategoryUseCase rename;
    private final ArchiveExpenseCategoryUseCase archive; private final ListExpenseCategoriesUseCase list;
    public ExpenseCategoryController(CreateExpenseCategoryUseCase create,RenameExpenseCategoryUseCase rename,
            ArchiveExpenseCategoryUseCase archive,ListExpenseCategoriesUseCase list){
        this.create=create;this.rename=rename;this.archive=archive;this.list=list;
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    CategoryResponse create(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,
            @Valid @RequestBody CategoryRequest request){var result=create.execute(user.id(),householdId,request.name());log("expense_category_created",householdId,result.id());return CategoryResponse.from(result);}
    @PutMapping("/{categoryId}")
    CategoryResponse rename(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,
            @PathVariable UUID categoryId,@Valid @RequestBody CategoryRequest request){
        var result=rename.execute(user.id(),householdId,categoryId,request.name());log("expense_category_renamed",householdId,categoryId);return CategoryResponse.from(result);
    }
    @PostMapping("/{categoryId}/archive")
    CategoryResponse archive(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,
            @PathVariable UUID categoryId){var result=archive.execute(user.id(),householdId,categoryId);log("expense_category_archived",householdId,categoryId);return CategoryResponse.from(result);}
    @GetMapping @Transactional(readOnly=true)
    List<CategoryResponse> list(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,
            @RequestParam(defaultValue="false") boolean includeArchived){
        return list.execute(user.id(),householdId,includeArchived).stream().map(CategoryResponse::from).toList();
    }
    record CategoryRequest(@NotBlank @Size(max=80) String name){}
    private static void log(String action,UUID householdId,UUID categoryId){LOG.atInfo().addKeyValue("event.action",action).addKeyValue("household.id",householdId).addKeyValue("expense.category.id",categoryId).log(action);}
    record CategoryResponse(UUID id,UUID householdId,UUID createdByMemberId,String name,ExpenseCategoryStatus status,
            Instant createdAt,Instant updatedAt,Instant archivedAt,long version){
        static CategoryResponse from(ExpenseCategoryView v){return new CategoryResponse(v.id(),v.householdId(),v.createdByMemberId(),
                v.name(),v.status(),v.createdAt(),v.updatedAt(),v.archivedAt(),v.version());}
    }
}
