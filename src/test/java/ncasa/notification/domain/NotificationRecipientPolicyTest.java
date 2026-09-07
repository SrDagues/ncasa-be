package ncasa.notification.domain;

import static org.junit.jupiter.api.Assertions.*;import java.util.*;import org.junit.jupiter.api.Test;

class NotificationRecipientPolicyTest {
    @Test void reminderTargetsActivePayerAndParticipantsWithoutDuplicates(){var creator=UUID.randomUUID();var payer=UUID.randomUUID();var participant=UUID.randomUUID();var inactive=UUID.randomUUID();var policy=new NotificationRecipientPolicy();var recipients=policy.recipients(NotificationKind.EXPENSE_PLAN_OCCURRENCE_APPROACHING,new NotificationRecipientPolicy.PlanAudience(creator,payer,Set.of(payer,participant,inactive)),List.of(new NotificationRecipientPolicy.Candidate(payer,1L,true,false),new NotificationRecipientPolicy.Candidate(participant,2L,true,false),new NotificationRecipientPolicy.Candidate(inactive,3L,false,false)));assertEquals(Set.of(new AccountRef(1L),new AccountRef(2L)),recipients);}
    @Test void attentionTargetsActiveCreatorAndAdministrators(){var creator=UUID.randomUUID();var admin=UUID.randomUUID();var member=UUID.randomUUID();var recipients=new NotificationRecipientPolicy().recipients(NotificationKind.EXPENSE_PLAN_ATTENTION_REQUIRED,new NotificationRecipientPolicy.PlanAudience(creator,member,Set.of(member)),List.of(new NotificationRecipientPolicy.Candidate(creator,1L,true,false),new NotificationRecipientPolicy.Candidate(admin,2L,true,true),new NotificationRecipientPolicy.Candidate(member,3L,true,false)));assertEquals(Set.of(new AccountRef(1L),new AccountRef(2L)),recipients);}
}
