package ncasa.shoppinglist.application;
import java.time.Clock;import java.util.UUID;import ncasa.shoppinglist.application.port.out.ShoppingListRepository;
public final class UnlinkShoppingListFromCalendarUseCase{private final ShoppingListRepository lists;private final Clock clock;public UnlinkShoppingListFromCalendarUseCase(ShoppingListRepository l,Clock c){lists=l;clock=c;}public void execute(UUID seriesId){lists.findActiveByCalendarSeries(seriesId).ifPresent(l->{l.unlinkCalendar(clock.instant());lists.save(l);});}}
