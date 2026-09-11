package ncasa.calendar.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name="calendar_entries")
class JpaCalendarEntryEntity {
    @Id UUID id;
    @Column(name="series_id",nullable=false) UUID seriesId;
    @Column(name="household_id",nullable=false) UUID householdId;
    @Column(nullable=false) String kind;
    @Column(nullable=false,length=240) String title;
    @Column(name="all_day",nullable=false) boolean allDay;
    @Column(name="start_date",nullable=false) LocalDate startDate;
    @Column(name="start_time") LocalTime startTime;
    @Column(name="end_date") LocalDate endDate;
    @Column(name="end_time") LocalTime endTime;
    @Column(name="zone_id",nullable=false) String zoneId="Europe/Madrid";
    @Column(nullable=false,length=7) String color;
    String location; String note; String link;
    @Column(name="recurrence_frequency") String recurrenceFrequency;
    @Column(name="recurrence_end_type") String recurrenceEndType;
    @Column(name="recurrence_until") LocalDate recurrenceUntil;
    @Column(name="recurrence_count") Integer recurrenceCount;
    @Column(name="special_date_type") String specialDateType;
    @Column(name="related_member_id") UUID relatedMemberId;
    @Column(name="created_by_member_id",nullable=false) UUID createdByMemberId;
    @Column(name="deleted_at") Instant deletedAt;
    @Column(name="created_at",nullable=false,updatable=false) Instant createdAt;
    @Column(name="updated_at",nullable=false) Instant updatedAt;
    @Version long version;

    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="calendar_entry_participants",joinColumns=@JoinColumn(name="entry_id"))
    @Column(name="member_id") Set<UUID> participants=new HashSet<>();
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="calendar_reminder_recipients",joinColumns=@JoinColumn(name="entry_id"))
    @Column(name="member_id") Set<UUID> reminderRecipients=new HashSet<>();
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="calendar_completed_occurrences",joinColumns=@JoinColumn(name="entry_id"))
    @Column(name="occurrence_key") Set<String> completedOccurrences=new HashSet<>();
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="calendar_entry_reminders",joinColumns=@JoinColumn(name="entry_id"))
    Set<JpaReminder> reminders=new HashSet<>();
    protected JpaCalendarEntryEntity(){}
}

@Embeddable
class JpaReminder {
    @Column(name="days_before") int daysBefore;
    @Column(nullable=false) boolean enabled;
    protected JpaReminder(){}
    JpaReminder(int daysBefore,boolean enabled){this.daysBefore=daysBefore;this.enabled=enabled;}
    @Override public boolean equals(Object other){return other instanceof JpaReminder r&&r.daysBefore==daysBefore;}
    @Override public int hashCode(){return Integer.hashCode(daysBefore);}
}
