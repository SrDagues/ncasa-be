package ncasa.notification.infrastructure.web;

import jakarta.validation.constraints.*;import java.time.*;import java.util.*;import ncasa.identityaccess.infrastructure.security.IdentityUserDetails;import ncasa.notification.application.*;import ncasa.notification.domain.NotificationKind;import org.slf4j.*;import org.springframework.http.HttpStatus;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.transaction.annotation.Transactional;import org.springframework.validation.annotation.Validated;import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/notifications") @Validated @Transactional
public class NotificationController{
    private static final Logger LOG=LoggerFactory.getLogger(NotificationController.class);
    private final ListNotificationsUseCase list;private final CountUnreadNotificationsUseCase count;
    private final MarkNotificationReadUseCase read;private final MarkAllNotificationsReadUseCase readAll;
    public NotificationController(ListNotificationsUseCase l,CountUnreadNotificationsUseCase c,MarkNotificationReadUseCase r,MarkAllNotificationsReadUseCase a){list=l;count=c;read=r;readAll=a;}
    @GetMapping @Transactional(readOnly=true) PageResponse list(@AuthenticationPrincipal IdentityUserDetails user,
            @RequestParam(defaultValue="false")boolean unreadOnly,@RequestParam(defaultValue="0")@Min(0)int page,
            @RequestParam(defaultValue="20")@Min(1)@Max(50)int size){return PageResponse.from(list.execute(user.id(),unreadOnly,page,size));}
    @GetMapping("/unread-count") @Transactional(readOnly=true) UnreadCountResponse unread(@AuthenticationPrincipal IdentityUserDetails user){return new UnreadCountResponse(count.execute(user.id()));}
    @PostMapping("/{notificationId}/read") NotificationResponse read(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID notificationId){var value=NotificationResponse.from(read.execute(user.id(),notificationId));LOG.atInfo().addKeyValue("event.action","notification_read").addKeyValue("notification.id",notificationId).log("notification_read");return value;}
    @PostMapping("/read-all") @ResponseStatus(HttpStatus.NO_CONTENT) void readAll(@AuthenticationPrincipal IdentityUserDetails user){int changed=readAll.execute(user.id());LOG.atInfo().addKeyValue("event.action","notifications_marked_read").addKeyValue("notification.changed_count",changed).log("notifications_marked_read");}
    record UnreadCountResponse(long unreadCount){}
    record PageResponse(List<NotificationResponse> items,int page,int size,long totalElements,int totalPages){static PageResponse from(NotificationPage p){return new PageResponse(p.items().stream().map(NotificationResponse::from).toList(),p.page(),p.size(),p.totalElements(),p.totalPages());}}
    record NotificationResponse(UUID id,NotificationKind kind,UUID householdId,UUID planId,UUID calendarEntryId,String subject,
            String amount,String currency,LocalDate occurrenceDate,Integer occurrenceNumber,Integer totalOccurrences,
            String attentionReason,UUID completedByMemberId,Instant occurredAt,Instant createdAt,Instant readAt){static NotificationResponse from(NotificationView v){return new NotificationResponse(v.id(),v.kind(),v.householdId(),v.planId(),v.calendarEntryId(),v.subject(),v.amount()==null?null:v.amount().toPlainString(),v.currency(),v.occurrenceDate(),v.occurrenceNumber(),v.totalOccurrences(),v.attentionReason(),v.completedByMemberId(),v.occurredAt(),v.createdAt(),v.readAt());}}
}
