package ncasa.expense.infrastructure.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("ncasa.outbox")public record OutboxProperties(int batchSize,int maxAttempts){public OutboxProperties{if(batchSize<1)batchSize=100;if(maxAttempts<1)maxAttempts=10;}}
