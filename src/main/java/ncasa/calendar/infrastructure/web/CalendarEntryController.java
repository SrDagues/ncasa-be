package ncasa.calendar.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import ncasa.calendar.application.*;
import ncasa.calendar.domain.*;
import ncasa.identityaccess.infrastructure.security.IdentityUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/households/{householdId}/calendar-items")
@Transactional
public class CalendarEntryController {
    private final CreateCalendarEntryUseCase create;private final UpdateCalendarEntryUseCase update;
    private final GetCalendarEntryUseCase get;private final ListCalendarOccurrencesUseCase list;
    private final ListTrashedCalendarEntriesUseCase trash;private final CalendarEntryLifecycleUseCase lifecycle;
    public CalendarEntryController(CreateCalendarEntryUseCase create,UpdateCalendarEntryUseCase update,
            GetCalendarEntryUseCase get,ListCalendarOccurrencesUseCase list,ListTrashedCalendarEntriesUseCase trash,
            CalendarEntryLifecycleUseCase lifecycle){this.create=create;this.update=update;this.get=get;this.list=list;this.trash=trash;this.lifecycle=lifecycle;}

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    EntryResponse create(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,
            @Valid @RequestBody EntryRequest request){return EntryResponse.from(create.execute(user.id(),householdId,request.command()));}

    @GetMapping("/{itemId}") @Transactional(readOnly=true)
    EntryResponse get(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,@PathVariable UUID itemId){return EntryResponse.from(get.execute(user.id(),householdId,itemId));}

    @GetMapping @Transactional(readOnly=true)
    List<OccurrenceResponse> list(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,
            @RequestParam LocalDate from,@RequestParam LocalDate to){return list.execute(user.id(),householdId,from,to).stream().map(OccurrenceResponse::from).toList();}

    @PutMapping("/{itemId}")
    EntryResponse update(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,@PathVariable UUID itemId,
            @RequestParam(required=false)LocalDate effectiveFrom,@Valid @RequestBody UpdateRequest request){
        return EntryResponse.from(update.execute(user.id(),householdId,itemId,request.version(),effectiveFrom,request.entry().command()));
    }

    @GetMapping("/trash") @Transactional(readOnly=true)
    List<EntryResponse> trash(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId){return trash.execute(user.id(),householdId).stream().map(EntryResponse::from).toList();}

    @PostMapping("/{itemId}/trash") EntryResponse trash(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,
            @PathVariable UUID itemId,@Valid @RequestBody VersionRequest request){return EntryResponse.from(lifecycle.trashFrom(user.id(),householdId,itemId,request.version(),request.effectiveFrom()));}
    @PostMapping("/{itemId}/restore") EntryResponse restore(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,
            @PathVariable UUID itemId,@Valid @RequestBody VersionRequest request){return EntryResponse.from(lifecycle.restore(user.id(),householdId,itemId,request.version()));}
    @DeleteMapping("/{itemId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void purge(@AuthenticationPrincipal IdentityUserDetails user,@PathVariable UUID householdId,@PathVariable UUID itemId,@RequestParam @PositiveOrZero long version){lifecycle.purge(user.id(),householdId,itemId,version);}
    @PostMapping("/{itemId}/occurrences/{occurrenceKey}/complete") EntryResponse complete(@AuthenticationPrincipal IdentityUserDetails user,
            @PathVariable UUID householdId,@PathVariable UUID itemId,@PathVariable String occurrenceKey,@Valid @RequestBody VersionRequest request){return EntryResponse.from(lifecycle.complete(user.id(),householdId,itemId,occurrenceKey,request.version()));}
    @PostMapping("/{itemId}/occurrences/{occurrenceKey}/reopen") EntryResponse reopen(@AuthenticationPrincipal IdentityUserDetails user,
            @PathVariable UUID householdId,@PathVariable UUID itemId,@PathVariable String occurrenceKey,@Valid @RequestBody VersionRequest request){return EntryResponse.from(lifecycle.reopen(user.id(),householdId,itemId,occurrenceKey,request.version()));}

    record VersionRequest(@PositiveOrZero long version,LocalDate effectiveFrom){}
    record UpdateRequest(@PositiveOrZero long version,@NotNull @Valid EntryRequest entry){}
    record EntryRequest(@NotNull CalendarEntryKind kind,@NotBlank@Size(max=240)String title,@NotNull@Valid TimingRequest timing,
            @NotBlank@Pattern(regexp="#[0-9a-fA-F]{6}")String color,@Size(max=240)String location,@Size(max=1000)String note,
            @Size(max=1000)String link,Set<UUID> participantMemberIds,@Valid RecurrenceRequest recurrence,
            List<@Valid ReminderRequest> reminders,Set<UUID> reminderRecipientMemberIds,SpecialDateType specialDateType,UUID relatedMemberId){
        CalendarEntryCommand command(){return new CalendarEntryCommand(kind,title,timing.value(),color,location,note,link,
                participantMemberIds,recurrence==null?null:recurrence.value(),reminders==null?List.of():reminders.stream().map(ReminderRequest::value).toList(),
                reminderRecipientMemberIds,specialDateType,relatedMemberId);}
    }
    record TimingRequest(boolean allDay,@NotNull LocalDate startDate,LocalTime startTime,LocalDate endDate,LocalTime endTime){
        CalendarTiming value(){return new CalendarTiming(allDay,startDate,startTime,endDate,endTime);}
    }
    record RecurrenceRequest(@NotNull RecurrenceFrequency frequency,@NotNull RecurrenceEndType endType,LocalDate untilDate,
            @Positive Integer totalOccurrences){RecurrenceRule value(){return new RecurrenceRule(frequency,endType,untilDate,totalOccurrences);}}
    record ReminderRequest(@Min(0)@Max(366)int daysBefore,boolean enabled){ReminderRule value(){return new ReminderRule(daysBefore,enabled);}}

    record EntryResponse(UUID id,UUID seriesId,UUID householdId,CalendarEntryKind kind,String title,CalendarTiming timing,
            String color,String location,String note,String link,Set<UUID> participantMemberIds,RecurrenceRule recurrence,
            List<ReminderRule> reminders,Set<UUID> reminderRecipientMemberIds,SpecialDateType specialDateType,UUID relatedMemberId,
            UUID createdByMemberId,Instant deletedAt,long version){
        static EntryResponse from(CalendarEntry e){return new EntryResponse(e.id(),e.seriesId(),e.householdId(),e.kind(),e.title(),e.timing(),e.color(),
                e.location(),e.note(),e.link(),e.participants(),e.recurrence(),e.reminders(),e.reminderRecipients(),e.specialDateType(),
                e.relatedMemberId(),e.createdByMemberId(),e.deletedAt(),e.version());}
    }
    record OccurrenceResponse(UUID itemId,String occurrenceKey,CalendarEntryKind kind,String title,CalendarTiming timing,String color,
            CalendarEntryStatus status,boolean recurring){static OccurrenceResponse from(CalendarOccurrence o){return new OccurrenceResponse(o.itemId(),o.occurrenceKey(),o.kind(),o.title(),o.timing(),o.color(),o.status(),o.recurring());}}
}
