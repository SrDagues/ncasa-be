package ncasa.expense.infrastructure.persistence;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataExpenseClassificationRepository extends JpaRepository<JpaExpenseClassificationChangeEntity,UUID>{
    List<JpaExpenseClassificationChangeEntity> findByExpenseIdAndHouseholdIdOrderByChangedAtAscIdAsc(UUID expenseId,UUID householdId);
}
