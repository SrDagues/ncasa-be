package ncasa.calendar.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import ncasa.calendar.application.port.out.CalendarEntryRepository;
import ncasa.calendar.application.port.out.CalendarHouseholdAccessPort;
import ncasa.calendar.application.port.out.TaskCompletionNotificationPort;
import ncasa.calendar.domain.*;
import org.junit.jupiter.api.Test;

class CalendarUseCasesTest {
    private final UUID household = UUID.randomUUID();
    private final UUID actorMember = UUID.randomUUID();
    private final UUID participant = UUID.randomUUID();
    private final Repository repository = new Repository();
    private final CalendarHouseholdAccessPort access = (householdId, accountId) ->
            new CalendarHouseholdContext(actorMember, Set.of(actorMember, participant));
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-09T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldCreateAndListAHouseholdEntry() {
        var created = new CreateCalendarEntryUseCase(repository, access, clock)
                .execute(1L, household, command(Set.of(participant)));

        var occurrences = new ListCalendarOccurrencesUseCase(repository, access)
                .execute(1L, household, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(created.createdByMemberId()).isEqualTo(actorMember);
        assertThat(occurrences).extracting(CalendarOccurrence::itemId).containsExactly(created.id());
    }

    @Test
    void shouldRejectParticipantsOutsideTheHousehold() {
        assertThatThrownBy(() -> new CreateCalendarEntryUseCase(repository, access, clock)
                .execute(1L, household, command(Set.of(UUID.randomUUID()))))
                .isInstanceOf(CalendarAccessDeniedException.class);
    }

    @Test
    void shouldTrashRestoreAndPermanentlyDelete() {
        var created = new CreateCalendarEntryUseCase(repository, access, clock).execute(1L, household, command(Set.of()));
        var lifecycle = new CalendarEntryLifecycleUseCase(repository, access, clock);

        lifecycle.trash(1L, household, created.id(), 0);
        assertThat(new ListCalendarOccurrencesUseCase(repository, access)
                .execute(1L, household, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))).isEmpty();
        assertThat(new ListTrashedCalendarEntriesUseCase(repository, access).execute(1L, household)).hasSize(1);

        lifecycle.restore(1L, household, created.id(), 0);
        lifecycle.trash(1L, household, created.id(), 0);
        lifecycle.purge(1L, household, created.id(), 0);
        assertThat(repository.entries).isEmpty();
    }

    @Test
    void shouldEditSelectedAndFollowingOccurrencesWithoutChangingHistory() {
        var recurring = new CalendarEntryCommand(CalendarEntryKind.TASK, "Original",
                CalendarTiming.allDay(LocalDate.of(2026, 9, 7), null), "#123456", null, null, null, Set.of(),
                RecurrenceRule.never(RecurrenceFrequency.WEEKLY), List.of(), Set.of(), null, null);
        var created = new CreateCalendarEntryUseCase(repository, access, clock).execute(1L, household, recurring);
        var replacement = new CalendarEntryCommand(CalendarEntryKind.TASK, "Nuevo",
                CalendarTiming.allDay(LocalDate.of(2026, 9, 21), null), "#654321", null, null, null, Set.of(),
                RecurrenceRule.never(RecurrenceFrequency.WEEKLY), List.of(), Set.of(), null, null);

        new UpdateCalendarEntryUseCase(repository, access).execute(1L, household, created.id(), 0,
                LocalDate.of(2026, 9, 21), replacement);

        assertThat(new ListCalendarOccurrencesUseCase(repository, access)
                .execute(1L, household, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
                .extracting(CalendarOccurrence::title).containsExactly("Original", "Original", "Nuevo", "Nuevo");
    }

    @Test
    void shouldNotifyWhenATaskOccurrenceIsCompletedButNotWhenItWasAlreadyCompleted() {
        var created = new CreateCalendarEntryUseCase(repository, access, clock)
                .execute(1L, household, command(Set.of(actorMember, participant)));
        var notifications = new CompletionNotifications();
        var lifecycle = new CalendarEntryLifecycleUseCase(repository, access, notifications, clock);

        lifecycle.complete(1L, household, created.id(), "2026-09-12", 0);
        lifecycle.complete(1L, household, created.id(), "2026-09-12", 0);

        assertThat(notifications.values).hasSize(1);
        assertThat(notifications.values.getFirst()).satisfies(notification -> {
            assertThat(notification.entryId()).isEqualTo(created.id());
            assertThat(notification.completedByMemberId()).isEqualTo(actorMember);
            assertThat(notification.participantMemberIds()).containsExactlyInAnyOrder(actorMember, participant);
            assertThat(notification.occurrenceDate()).isEqualTo(LocalDate.of(2026, 9, 12));
            assertThat(notification.title()).isEqualTo("Comprar");
        });
    }

    private CalendarEntryCommand command(Set<UUID> participants) {
        return new CalendarEntryCommand(CalendarEntryKind.TASK, "Comprar", CalendarTiming.allDay(
                LocalDate.of(2026, 9, 12), null), "#123456", null, null, null, participants, null,
                List.of(new ReminderRule(1, true)), participants, null, null);
    }

    private static final class Repository implements CalendarEntryRepository {
        private final List<CalendarEntry> entries = new ArrayList<>();
        @Override public CalendarEntry save(CalendarEntry entry) { entries.removeIf(x -> x.id().equals(entry.id())); entries.add(entry); return entry; }
        @Override public Optional<CalendarEntry> find(UUID id, UUID householdId) { return entries.stream().filter(x -> x.id().equals(id)&&x.householdId().equals(householdId)).findFirst(); }
        @Override public List<CalendarEntry> findActive(UUID householdId) { return entries.stream().filter(x -> x.householdId().equals(householdId)&&x.deletedAt()==null).toList(); }
        @Override public List<CalendarEntry> findTrashed(UUID householdId) { return entries.stream().filter(x -> x.householdId().equals(householdId)&&x.deletedAt()!=null).toList(); }
        @Override public void delete(CalendarEntry entry) { entries.removeIf(x -> x.id().equals(entry.id())); }
    }

    private static final class CompletionNotifications implements TaskCompletionNotificationPort {
        private final List<TaskCompleted> values = new ArrayList<>();
        @Override public void notify(TaskCompleted notification) { values.add(notification); }
    }
}
