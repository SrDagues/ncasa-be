package ncasa.household.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import ncasa.household.application.*;
import ncasa.household.application.accept.*;
import ncasa.household.application.archive.ArchiveHouseholdUseCase;
import ncasa.household.application.create.CreateHouseholdUseCase;
import ncasa.household.application.get.*;
import ncasa.household.application.invite.*;
import ncasa.household.application.member.*;
import ncasa.household.application.rename.RenameHouseholdUseCase;
import ncasa.household.domain.*;
import ncasa.identityaccess.infrastructure.security.IdentityUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Transactional
public class HouseholdController {
    private final CreateHouseholdUseCase create; private final GetHouseholdUseCase get;
    private final GetHouseholdMembersUseCase getMembers; private final RenameHouseholdUseCase rename;
    private final ChangeMemberRoleUseCase changeRole; private final TransferOwnershipUseCase transfer;
    private final RemoveMemberUseCase remove; private final LeaveHouseholdUseCase leave;
    private final ArchiveHouseholdUseCase archive; private final InviteHouseholdMemberUseCase invite;
    private final CancelHouseholdInvitationUseCase cancel; private final GetPendingInvitationsUseCase pending;
    private final AcceptInvitationByIdUseCase acceptById; private final AcceptInvitationByTokenUseCase acceptByToken;
    private final ListAccountHouseholdsUseCase list; private final GetHouseholdPendingInvitationsUseCase sent;

    public HouseholdController(CreateHouseholdUseCase create, GetHouseholdUseCase get,
            GetHouseholdMembersUseCase getMembers, RenameHouseholdUseCase rename, ChangeMemberRoleUseCase changeRole,
            TransferOwnershipUseCase transfer, RemoveMemberUseCase remove, LeaveHouseholdUseCase leave,
            ArchiveHouseholdUseCase archive, InviteHouseholdMemberUseCase invite,
            CancelHouseholdInvitationUseCase cancel, GetPendingInvitationsUseCase pending,
            AcceptInvitationByIdUseCase acceptById, AcceptInvitationByTokenUseCase acceptByToken,
            ListAccountHouseholdsUseCase list, GetHouseholdPendingInvitationsUseCase sent) {
        this.create = create; this.get = get; this.getMembers = getMembers; this.rename = rename;
        this.changeRole = changeRole; this.transfer = transfer; this.remove = remove; this.leave = leave;
        this.archive = archive; this.invite = invite; this.cancel = cancel; this.pending = pending;
        this.acceptById = acceptById; this.acceptByToken = acceptByToken;
        this.list = list; this.sent = sent;
    }

    @GetMapping("/api/households") @Transactional(readOnly = true)
    List<HouseholdSummaryView> list(@AuthenticationPrincipal IdentityUserDetails user) {
        return list.execute(account(user));
    }

    @PostMapping("/api/households") @ResponseStatus(HttpStatus.CREATED)
    HouseholdView create(@AuthenticationPrincipal IdentityUserDetails user, @Valid @RequestBody CreateRequest request) {
        return create.execute(account(user), request.name());
    }
    @GetMapping("/api/households/{id}") @Transactional(readOnly = true)
    HouseholdView get(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id) {
        return get.execute(new HouseholdId(id), account(user));
    }
    @GetMapping("/api/households/{id}/members") @Transactional(readOnly = true)
    List<MemberView> members(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id) {
        return getMembers.execute(new HouseholdId(id), account(user));
    }
    @PatchMapping("/api/households/{id}")
    HouseholdView rename(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id,
            @Valid @RequestBody RenameRequest request) { return rename.execute(new HouseholdId(id), account(user), request.name()); }
    @PatchMapping("/api/households/{id}/members/{memberId}/role")
    HouseholdView changeRole(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id,
            @PathVariable UUID memberId, @Valid @RequestBody RoleRequest request) {
        return changeRole.execute(new HouseholdId(id), account(user), new MemberId(memberId), request.role());
    }
    @PostMapping("/api/households/{id}/ownership-transfers")
    HouseholdView transfer(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id,
            @Valid @RequestBody TransferRequest request) {
        return transfer.execute(new HouseholdId(id), account(user), new MemberId(request.memberId()));
    }
    @DeleteMapping("/api/households/{id}/members/{memberId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id, @PathVariable UUID memberId) {
        remove.execute(new HouseholdId(id), account(user), new MemberId(memberId));
    }
    @PostMapping("/api/households/{id}/leave") @ResponseStatus(HttpStatus.NO_CONTENT)
    void leave(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id) { leave.execute(new HouseholdId(id), account(user)); }
    @DeleteMapping("/api/households/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void archive(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id) { archive.execute(new HouseholdId(id), account(user)); }
    @PostMapping("/api/households/{id}/invitations") @ResponseStatus(HttpStatus.CREATED)
    InvitationResult invite(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id,
            @Valid @RequestBody InviteRequest request) {
        return invite.execute(new HouseholdId(id), account(user), request.email(), request.role());
    }
    @DeleteMapping("/api/households/{id}/invitations/{invitationId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancel(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id, @PathVariable UUID invitationId) {
        cancel.execute(new HouseholdId(id), new InvitationId(invitationId), account(user));
    }
    @GetMapping("/api/households/{id}/invitations")
    List<SentInvitationView> sent(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id,
            @RequestParam(defaultValue = "PENDING") InvitationStatus status) {
        if (status != InvitationStatus.PENDING) throw new IllegalArgumentException("Only PENDING invitations can be listed");
        return sent.execute(new HouseholdId(id), account(user));
    }
    @GetMapping("/api/household-invitations/pending")
    List<InvitationView> pending(@AuthenticationPrincipal IdentityUserDetails user) { return pending.execute(user.email()); }
    @PostMapping("/api/household-invitations/{id}/accept")
    HouseholdView acceptById(@AuthenticationPrincipal IdentityUserDetails user, @PathVariable UUID id) {
        return acceptById.execute(new InvitationId(id), account(user), user.email());
    }
    @PostMapping("/api/household-invitations/accept-by-token")
    HouseholdView acceptByToken(@AuthenticationPrincipal IdentityUserDetails user, @Valid @RequestBody TokenRequest request) {
        return acceptByToken.execute(request.token(), account(user), user.email());
    }

    private AccountId account(IdentityUserDetails user) { return new AccountId(user.id()); }
    record CreateRequest(@NotBlank @Size(max = 120) String name) {}
    record RenameRequest(@NotBlank @Size(max = 120) String name) {}
    record RoleRequest(@NotNull HouseholdRole role) {}
    record TransferRequest(@NotNull UUID memberId) {}
    record InviteRequest(@NotBlank @Email @Size(max = 320) String email, @NotNull HouseholdRole role) {}
    record TokenRequest(@NotBlank @Size(max = 512) String token) {}
}
