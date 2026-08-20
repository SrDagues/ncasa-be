package ncasa.household.infrastructure.config;

import java.time.Clock;
import ncasa.household.application.accept.*;
import ncasa.household.application.HouseholdViewAssembler;
import ncasa.household.application.archive.ArchiveHouseholdUseCase;
import ncasa.household.application.create.CreateHouseholdUseCase;
import ncasa.household.application.get.*;
import ncasa.household.application.invite.*;
import ncasa.household.application.member.*;
import ncasa.household.application.port.out.*;
import ncasa.household.application.rename.RenameHouseholdUseCase;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HouseholdInvitationProperties.class)
public class HouseholdConfiguration {
    @Bean HouseholdViewAssembler householdViews(AccountDirectoryPort accounts) { return new HouseholdViewAssembler(accounts); }
    @Bean CreateHouseholdUseCase createHousehold(HouseholdRepository r, Clock c, HouseholdViewAssembler v) { return new CreateHouseholdUseCase(r, c, v); }
    @Bean GetHouseholdUseCase getHousehold(HouseholdRepository r, HouseholdViewAssembler v) { return new GetHouseholdUseCase(r, v); }
    @Bean GetHouseholdMembersUseCase getMembers(HouseholdRepository r, HouseholdViewAssembler v) { return new GetHouseholdMembersUseCase(r, v); }
    @Bean ListAccountHouseholdsUseCase listHouseholds(HouseholdRepository r) { return new ListAccountHouseholdsUseCase(r); }
    @Bean RenameHouseholdUseCase renameHousehold(HouseholdRepository r, Clock c, HouseholdViewAssembler v) { return new RenameHouseholdUseCase(r, c, v); }
    @Bean ChangeMemberRoleUseCase changeRole(HouseholdRepository r, Clock c, HouseholdViewAssembler v) { return new ChangeMemberRoleUseCase(r, c, v); }
    @Bean TransferOwnershipUseCase transferOwnership(HouseholdRepository r, Clock c, HouseholdViewAssembler v) { return new TransferOwnershipUseCase(r, c, v); }
    @Bean RemoveMemberUseCase removeMember(HouseholdRepository r, Clock c) { return new RemoveMemberUseCase(r, c); }
    @Bean LeaveHouseholdUseCase leaveHousehold(HouseholdRepository r, Clock c) { return new LeaveHouseholdUseCase(r, c); }
    @Bean ArchiveHouseholdUseCase archiveHousehold(HouseholdRepository r, Clock c) { return new ArchiveHouseholdUseCase(r, c); }
    @Bean InviteHouseholdMemberUseCase invite(HouseholdRepository h, HouseholdInvitationRepository i,
            InvitationTokenGenerator g, InvitationTokenHasher s, InvitationDeliveryPort d, Clock c,
            HouseholdInvitationProperties p) { return new InviteHouseholdMemberUseCase(h, i, g, s, d, c, p.expiration()); }
    @Bean CancelHouseholdInvitationUseCase cancelInvitation(HouseholdRepository h, HouseholdInvitationRepository i, Clock c) {
        return new CancelHouseholdInvitationUseCase(h, i, c);
    }
    @Bean GetPendingInvitationsUseCase pendingInvitations(HouseholdRepository h, HouseholdInvitationRepository i,
            AccountDirectoryPort a, Clock c) {
        return new GetPendingInvitationsUseCase(h, i, a, c);
    }
    @Bean GetHouseholdPendingInvitationsUseCase householdPendingInvitations(HouseholdRepository h,
            HouseholdInvitationRepository i, Clock c) {
        return new GetHouseholdPendingInvitationsUseCase(h, i, c);
    }
    @Bean InvitationAcceptanceService acceptance(HouseholdRepository h, HouseholdInvitationRepository i, Clock c) {
        return new InvitationAcceptanceService(h, i, c);
    }
    @Bean AcceptInvitationByIdUseCase acceptById(HouseholdInvitationRepository i, InvitationAcceptanceService a,
            HouseholdViewAssembler v) {
        return new AcceptInvitationByIdUseCase(i, a, v);
    }
    @Bean AcceptInvitationByTokenUseCase acceptByToken(HouseholdInvitationRepository i, InvitationTokenHasher h,
            InvitationAcceptanceService a, HouseholdViewAssembler v) { return new AcceptInvitationByTokenUseCase(i, h, a, v); }
}
