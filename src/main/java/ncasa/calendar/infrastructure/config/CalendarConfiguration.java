package ncasa.calendar.infrastructure.config;

import java.time.Clock;
import ncasa.calendar.application.*;
import ncasa.calendar.application.port.out.*;
import ncasa.calendar.infrastructure.household.HouseholdCalendarAccessAdapter;
import ncasa.household.application.get.GetHouseholdMembershipContextUseCase;
import org.springframework.context.annotation.*;

@Configuration
public class CalendarConfiguration {
    @Bean CalendarHouseholdAccessPort calendarHouseholdAccess(GetHouseholdMembershipContextUseCase h){return new HouseholdCalendarAccessAdapter(h);}
    @Bean CreateCalendarEntryUseCase createCalendarEntry(CalendarEntryRepository r,CalendarHouseholdAccessPort h,Clock c){return new CreateCalendarEntryUseCase(r,h,c);}
    @Bean ListCalendarOccurrencesUseCase listCalendarOccurrences(CalendarEntryRepository r,CalendarHouseholdAccessPort h){return new ListCalendarOccurrencesUseCase(r,h);}
    @Bean ListTrashedCalendarEntriesUseCase listTrashedCalendarEntries(CalendarEntryRepository r,CalendarHouseholdAccessPort h){return new ListTrashedCalendarEntriesUseCase(r,h);}
    @Bean CalendarEntryLifecycleUseCase calendarEntryLifecycle(CalendarEntryRepository r,CalendarHouseholdAccessPort h,TaskCompletionNotificationPort n,Clock c){return new CalendarEntryLifecycleUseCase(r,h,n,c);}
    @Bean UpdateCalendarEntryUseCase updateCalendarEntry(CalendarEntryRepository r,CalendarHouseholdAccessPort h){return new UpdateCalendarEntryUseCase(r,h);}
    @Bean GetCalendarEntryUseCase getCalendarEntry(CalendarEntryRepository r,CalendarHouseholdAccessPort h){return new GetCalendarEntryUseCase(r,h);}
}
