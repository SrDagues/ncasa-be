package ncasa.expense.infrastructure.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("ncasa.expense-plans")public record ExpensePlanProperties(int batchSize){public ExpensePlanProperties{if(batchSize<1)batchSize=100;}}
