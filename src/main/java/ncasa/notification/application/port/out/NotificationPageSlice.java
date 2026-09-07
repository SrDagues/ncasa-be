package ncasa.notification.application.port.out;

import java.util.List;import ncasa.notification.domain.Notification;
public record NotificationPageSlice(List<Notification> content,long totalElements){public NotificationPageSlice{content=List.copyOf(content);}}
