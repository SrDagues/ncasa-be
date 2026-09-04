package ncasa.expense.application.port.out;
import java.time.*;import java.util.*;
public interface OutboxMessageRepository{List<OutboxMessage> claim(Instant now,int batchSize);void published(UUID id,Instant now);void retry(UUID id,Instant availableAt,String error,boolean failed);}
