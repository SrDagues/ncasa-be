package ncasa.calendar.infrastructure.persistence;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCalendarEntryRepository extends JpaRepository<JpaCalendarEntryEntity,UUID> {
    Optional<JpaCalendarEntryEntity> findByIdAndHouseholdId(UUID id,UUID householdId);
    List<JpaCalendarEntryEntity> findByHouseholdIdAndDeletedAtIsNullOrderByStartDateAscIdAsc(UUID householdId);
    List<JpaCalendarEntryEntity> findByHouseholdIdAndDeletedAtIsNotNullOrderByDeletedAtDescIdAsc(UUID householdId);
}
