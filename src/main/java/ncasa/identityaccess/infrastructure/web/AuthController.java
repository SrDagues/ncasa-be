package ncasa.identityaccess.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import ncasa.identityaccess.application.AuthenticationResult;
import ncasa.identityaccess.application.RegistrationResult;
import ncasa.identityaccess.application.InvalidRefreshTokenException;
import ncasa.identityaccess.application.login.LoginUserUseCase;
import ncasa.identityaccess.application.logout.LogoutUserUseCase;
import ncasa.identityaccess.application.refresh.RefreshSessionUseCase;
import ncasa.identityaccess.application.register.RegisterUserUseCase;
import ncasa.identityaccess.application.verification.ConfirmEmailUseCase;
import ncasa.identityaccess.application.verification.ResendEmailVerificationUseCase;
import ncasa.identityaccess.infrastructure.security.IdentityUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final RegisterUserUseCase registerUser;
    private final LoginUserUseCase loginUser;
    private final RefreshSessionUseCase refreshSession;
    private final LogoutUserUseCase logoutUser;
    private final ConfirmEmailUseCase confirmEmail;
    private final ResendEmailVerificationUseCase resendEmailVerification;
    private final PublicAuthRateLimiter rateLimiter;
    private final RefreshTokenCookieFactory refreshCookies;

    public AuthController(RegisterUserUseCase registerUser, LoginUserUseCase loginUser,
            RefreshSessionUseCase refreshSession, LogoutUserUseCase logoutUser,
            ConfirmEmailUseCase confirmEmail, ResendEmailVerificationUseCase resendEmailVerification,
            PublicAuthRateLimiter rateLimiter, RefreshTokenCookieFactory refreshCookies) {
        this.registerUser = registerUser;
        this.loginUser = loginUser;
        this.refreshSession = refreshSession;
        this.logoutUser = logoutUser;
        this.confirmEmail = confirmEmail;
        this.resendEmailVerification = resendEmailVerification;
        this.rateLimiter = rateLimiter;
        this.refreshCookies = refreshCookies;
    }

    @PostMapping("/register")
    @Transactional
    ResponseEntity<RegistrationResult> register(@Valid @RequestBody RegisterRequest request) {
        RegistrationResult result = registerUser.execute(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result);
    }

    @PostMapping("/email-verification")
    @Transactional
    ResponseEntity<Void> confirmEmail(@Valid @RequestBody ConfirmEmailRequest request,
            HttpServletRequest servletRequest) {
        rateLimiter.checkConfirmation(servletRequest.getRemoteAddr());
        confirmEmail.execute(request.token());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/email-verification/resend")
    @Transactional
    ResponseEntity<Void> resendEmailVerification(@Valid @RequestBody ResendEmailVerificationRequest request,
            HttpServletRequest servletRequest) {
        rateLimiter.checkResend(servletRequest.getRemoteAddr());
        resendEmailVerification.execute(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/login")
    @Transactional
    ResponseEntity<WebAuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResult result = loginUser.execute(request.email(), request.password());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookies.create(result.refreshToken()).toString())
                .body(WebAuthenticationResponse.from(result));
    }

    @PostMapping("/refresh")
    @Transactional
    ResponseEntity<WebAuthenticationResponse> refresh(
            @CookieValue(name = "${app.auth.refresh-cookie.name}", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }
        AuthenticationResult result = refreshSession.execute(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookies.create(result.refreshToken()).toString())
                .body(WebAuthenticationResponse.from(result));
    }

    @GetMapping("/me")
    UserResponse me(@AuthenticationPrincipal IdentityUserDetails user) {
        return new UserResponse(user.id(), user.email(),
                user.getAuthorities().stream().map(Object::toString).sorted().toList());
    }

    @PostMapping("/logout")
    @Transactional
    ResponseEntity<Void> logout(
            @CookieValue(name = "${app.auth.refresh-cookie.name}", required = false) String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            logoutUser.execute(refreshToken);
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookies.expire().toString())
                .build();
    }

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 8, max = 128) String password) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record ConfirmEmailRequest(@NotBlank @Size(max = 128) String token) {}
    public record ResendEmailVerificationRequest(@NotBlank @Email @Size(max = 320) String email) {}
    public record UserResponse(Long id, String email, List<String> roles) {}

}
