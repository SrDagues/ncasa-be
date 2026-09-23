package ncasa.identityaccess.infrastructure.scheduling;

import java.time.Clock;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenRepository;
import ncasa.identityaccess.infrastructure.config.EmailVerificationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationTokenCleanupJob {
    private final EmailVerificationTokenRepository tokens;
    private final EmailVerificationProperties properties;
    private final Clock clock;

    public EmailVerificationTokenCleanupJob(EmailVerificationTokenRepository tokens,
            EmailVerificationProperties properties, Clock clock) {
        this.tokens = tokens;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(cron = "${ncasa.identity-access.email-verification.cleanup-cron:0 15 3 * * *}")
    public void clean() {
        tokens.deleteTerminalBefore(clock.instant().minus(properties.tokenRetention()));
    }
}
