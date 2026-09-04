package ncasa.notification.application;
import java.util.List;
public record NotificationPage(List<NotificationView> items,int page,int size,long totalElements,int totalPages){public NotificationPage{items=List.copyOf(items);}}
