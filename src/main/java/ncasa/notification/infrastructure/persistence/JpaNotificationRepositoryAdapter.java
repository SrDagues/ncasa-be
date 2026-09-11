package ncasa.notification.infrastructure.persistence;

import java.time.Instant;import java.util.*;import java.util.stream.Collectors;import ncasa.notification.application.port.out.*;import ncasa.notification.domain.*;import org.springframework.data.domain.PageRequest;import org.springframework.stereotype.Repository;

@Repository
public class JpaNotificationRepositoryAdapter implements NotificationRepository {
    private final SpringDataNotificationRepository repository;
    public JpaNotificationRepositoryAdapter(SpringDataNotificationRepository repository){this.repository=repository;}
    public boolean exists(IntegrationEventId eventId,AccountRef recipient){return repository.existsByEventIdAndRecipientAccountId(eventId.value(),recipient.value());}
    public void saveAll(List<Notification> values){repository.saveAllAndFlush(values.stream().map(this::entity).toList());}
    public Notification save(Notification value){return domain(repository.saveAndFlush(entity(value)));}
    public NotificationPageSlice findPage(AccountRef account,Set<HouseholdRef> households,boolean unreadOnly,int page,int size){
        if(households.isEmpty())return new NotificationPageSlice(List.of(),0);
        var result=repository.page(account.value(),ids(households),unreadOnly,PageRequest.of(page,size));
        return new NotificationPageSlice(result.getContent().stream().map(this::domain).toList(),result.getTotalElements());
    }
    public long countUnread(AccountRef account,Set<HouseholdRef> households){return households.isEmpty()?0:repository.unread(account.value(),ids(households));}
    public Optional<Notification> findAccessible(NotificationId id,AccountRef account,Set<HouseholdRef> households){return households.isEmpty()?Optional.empty():repository.accessible(id.value(),account.value(),ids(households)).map(this::domain);}
    public int markAllRead(AccountRef account,Set<HouseholdRef> households,Instant at){return households.isEmpty()?0:repository.markAllRead(account.value(),ids(households),at);}
    private Set<UUID> ids(Set<HouseholdRef> values){return values.stream().map(HouseholdRef::value).collect(Collectors.toUnmodifiableSet());}
    private JpaNotificationEntity entity(Notification n){var e=new JpaNotificationEntity();e.id=n.id().value();e.eventId=n.eventId().value();e.recipientAccountId=n.recipient().value();e.householdId=n.householdId().value();e.planId=n.planId()==null?null:n.planId().value();e.calendarEntryId=n.calendarEntryId()==null?null:n.calendarEntryId().value();e.kind=n.kind().name();e.subject=n.subject();e.amount=n.amount()==null?null:n.amount().amount();e.currency=n.amount()==null?null:n.amount().currency();e.occurrenceDate=n.occurrenceDate();e.occurrenceNumber=n.occurrenceNumber();e.totalOccurrences=n.totalOccurrences();e.attentionReason=n.attentionReason();e.completedByMemberId=n.completedByMemberId();e.occurredAt=n.occurredAt();e.createdAt=n.createdAt();e.readAt=n.readAt();return e;}
    private Notification domain(JpaNotificationEntity e){return Notification.rehydrate(new NotificationId(e.id),new IntegrationEventId(e.eventId),new AccountRef(e.recipientAccountId),new HouseholdRef(e.householdId),e.planId==null?null:new PlanRef(e.planId),e.calendarEntryId==null?null:new CalendarEntryRef(e.calendarEntryId),NotificationKind.valueOf(e.kind),e.subject,e.amount==null?null:new NotificationAmount(e.amount,e.currency),e.occurrenceDate,e.occurrenceNumber,e.totalOccurrences,e.attentionReason,e.completedByMemberId,e.occurredAt,e.createdAt,e.readAt);}
}
