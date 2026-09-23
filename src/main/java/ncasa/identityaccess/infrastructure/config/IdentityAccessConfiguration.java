package ncasa.identityaccess.infrastructure.config;

import java.time.Clock;
import ncasa.identityaccess.application.login.LoginUserUseCase;
import ncasa.identityaccess.application.logout.LogoutUserUseCase;
import ncasa.identityaccess.application.port.out.AccessTokenIssuer;
import ncasa.identityaccess.application.port.out.AuthSessionRepository;
import ncasa.identityaccess.application.port.out.PasswordHasher;
import ncasa.identityaccess.application.port.out.RefreshTokenGenerator;
import ncasa.identityaccess.application.port.out.TokenHasher;
import ncasa.identityaccess.application.port.out.UserAccountRepository;
import ncasa.identityaccess.application.port.out.EmailVerificationEventPublisher;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenGenerator;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenHasher;
import ncasa.identityaccess.application.port.out.EmailVerificationTokenRepository;
import ncasa.identityaccess.application.port.out.TransactionalEmailSender;
import ncasa.identityaccess.application.refresh.RefreshSessionUseCase;
import ncasa.identityaccess.application.register.RegisterUserUseCase;
import ncasa.identityaccess.application.session.SessionIssuer;
import ncasa.identityaccess.application.verification.ConfirmEmailUseCase;
import ncasa.identityaccess.application.verification.EmailVerificationTemplate;
import ncasa.identityaccess.application.verification.IssueEmailVerification;
import ncasa.identityaccess.application.verification.ResendEmailVerificationUseCase;
import ncasa.identityaccess.application.verification.SendEmailVerificationHandler;
import ncasa.identityaccess.infrastructure.security.JwtProperties;
import ncasa.identityaccess.infrastructure.web.RefreshCookieProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RefreshCookieProperties.class, EmailVerificationProperties.class,
        ResendProperties.class})
public class IdentityAccessConfiguration {
    @Bean
    SessionIssuer sessionIssuer(AuthSessionRepository sessions, RefreshTokenGenerator refreshTokens,
            TokenHasher tokenHasher, AccessTokenIssuer accessTokens, Clock clock, JwtProperties jwtProperties) {
        return new SessionIssuer(sessions, refreshTokens, tokenHasher, accessTokens, clock,
                jwtProperties.refreshTokenExpiration());
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(UserAccountRepository users, PasswordHasher passwordHasher,
            IssueEmailVerification verification, Clock clock) {
        return new RegisterUserUseCase(users, passwordHasher, verification, clock);
    }

    @Bean
    IssueEmailVerification issueEmailVerification(EmailVerificationTokenRepository tokens,
            EmailVerificationTokenGenerator generator, EmailVerificationTokenHasher hasher,
            EmailVerificationEventPublisher events, EmailVerificationProperties properties) {
        return new IssueEmailVerification(tokens, generator, hasher, events, properties.tokenTtl());
    }

    @Bean
    ConfirmEmailUseCase confirmEmailUseCase(UserAccountRepository users, EmailVerificationTokenRepository tokens,
            EmailVerificationTokenHasher hasher, Clock clock) {
        return new ConfirmEmailUseCase(users, tokens, hasher, clock);
    }

    @Bean
    ResendEmailVerificationUseCase resendEmailVerificationUseCase(UserAccountRepository users,
            EmailVerificationTokenRepository tokens, IssueEmailVerification issuer, Clock clock,
            EmailVerificationProperties properties) {
        return new ResendEmailVerificationUseCase(users, tokens, issuer, clock, properties.resendCooldown(),
                properties.maxSendsPer24h());
    }

    @Bean
    EmailVerificationTemplate emailVerificationTemplate(EmailVerificationProperties properties) {
        return new EmailVerificationTemplate(properties.frontendUrl());
    }

    @Bean
    SendEmailVerificationHandler sendEmailVerificationHandler(EmailVerificationTemplate template,
            TransactionalEmailSender sender) {
        return new SendEmailVerificationHandler(template, sender);
    }

    @Bean
    LoginUserUseCase loginUserUseCase(UserAccountRepository users, PasswordHasher passwordHasher,
            SessionIssuer sessions) {
        return new LoginUserUseCase(users, passwordHasher, sessions);
    }

    @Bean
    RefreshSessionUseCase refreshSessionUseCase(AuthSessionRepository sessions, UserAccountRepository users,
            TokenHasher tokenHasher, SessionIssuer issuer, Clock clock) {
        return new RefreshSessionUseCase(sessions, users, tokenHasher, issuer, clock);
    }

    @Bean
    LogoutUserUseCase logoutUserUseCase(AuthSessionRepository sessions, TokenHasher tokenHasher) {
        return new LogoutUserUseCase(sessions, tokenHasher);
    }
}
