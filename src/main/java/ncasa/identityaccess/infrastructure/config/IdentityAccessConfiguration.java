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
import ncasa.identityaccess.application.refresh.RefreshSessionUseCase;
import ncasa.identityaccess.application.register.RegisterUserUseCase;
import ncasa.identityaccess.application.session.SessionIssuer;
import ncasa.identityaccess.infrastructure.security.JwtProperties;
import ncasa.identityaccess.infrastructure.web.RefreshCookieProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RefreshCookieProperties.class)
public class IdentityAccessConfiguration {
    @Bean
    SessionIssuer sessionIssuer(AuthSessionRepository sessions, RefreshTokenGenerator refreshTokens,
            TokenHasher tokenHasher, AccessTokenIssuer accessTokens, Clock clock, JwtProperties jwtProperties) {
        return new SessionIssuer(sessions, refreshTokens, tokenHasher, accessTokens, clock,
                jwtProperties.refreshTokenExpiration());
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(UserAccountRepository users, PasswordHasher passwordHasher,
            SessionIssuer sessions, Clock clock) {
        return new RegisterUserUseCase(users, passwordHasher, sessions, clock);
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
