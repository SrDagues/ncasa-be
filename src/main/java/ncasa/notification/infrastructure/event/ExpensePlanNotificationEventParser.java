package ncasa.notification.infrastructure.event;

import java.math.BigDecimal;import java.time.*;import java.util.*;import ncasa.expense.infrastructure.outbox.PublishedOutboxEvent;import ncasa.notification.application.ConsumeExpensePlanNotificationCommand;import ncasa.notification.domain.NotificationKind;import tools.jackson.databind.*;import org.springframework.stereotype.Component;

@Component
public class ExpensePlanNotificationEventParser {
    private static final Map<String,NotificationKind> SUPPORTED=Map.of(
            "ExpensePlanOccurrenceApproaching",NotificationKind.EXPENSE_PLAN_OCCURRENCE_APPROACHING,
            "ExpensePlanLastInstallmentApproaching",NotificationKind.EXPENSE_PLAN_LAST_INSTALLMENT_APPROACHING,
            "ExpensePlanAttentionRequired",NotificationKind.EXPENSE_PLAN_ATTENTION_REQUIRED);
    private final ObjectMapper json;public ExpensePlanNotificationEventParser(ObjectMapper json){this.json=json;}
    public Optional<ConsumeExpensePlanNotificationCommand> parse(PublishedOutboxEvent event){
        var kind=SUPPORTED.get(event.eventType());if(kind==null)return Optional.empty();
        try{
            JsonNode root=json.readTree(event.payload());
            if(!event.eventId().toString().equals(text(root,"eventId")))throw new IllegalArgumentException("Event id mismatch");
            if(!event.eventType().equals(text(root,"eventType")))throw new IllegalArgumentException("Event type mismatch");
            if(!event.occurredAt().equals(Instant.parse(text(root,"occurredAt"))))throw new IllegalArgumentException("Event timestamp mismatch");
            var data=required(root,"data");var total=optionalInt(data,"totalOccurrences");
            return Optional.of(new ConsumeExpensePlanNotificationCommand(event.eventId(),kind,
                    UUID.fromString(text(data,"householdId")),UUID.fromString(text(data,"planId")),
                    new BigDecimal(text(data,"amount")),text(data,"currency"),LocalDate.parse(text(root,"occurrenceDate")),
                    integer(data,"occurrenceNumber"),total,event.occurredAt()));
        }catch(IllegalArgumentException ex){throw ex;}catch(RuntimeException ex){throw new IllegalArgumentException("Malformed expense-plan notification event",ex);}
    }
    private JsonNode required(JsonNode node,String field){var value=node==null?null:node.get(field);if(value==null||value.isNull())throw new IllegalArgumentException("Missing event field: "+field);return value;}
    private String text(JsonNode node,String field){var value=required(node,field);if(!value.isString()||value.asString().isBlank())throw new IllegalArgumentException("Invalid event field: "+field);return value.asString();}
    private int integer(JsonNode node,String field){var value=required(node,field);if(!value.isIntegralNumber())throw new IllegalArgumentException("Invalid event field: "+field);return value.asInt();}
    private Integer optionalInt(JsonNode node,String field){var value=node.get(field);if(value==null||value.isNull())return null;if(!value.isIntegralNumber())throw new IllegalArgumentException("Invalid event field: "+field);return value.asInt();}
}
