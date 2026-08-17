package ncasa.identityaccess.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import ncasa.identityaccess.application.AuthenticatedUser;
import ncasa.identityaccess.application.AuthenticationResult;
import ncasa.identityaccess.application.login.LoginUserUseCase;
import ncasa.identityaccess.application.logout.LogoutUserUseCase;
import ncasa.identityaccess.application.refresh.RefreshSessionUseCase;
import ncasa.identityaccess.application.register.RegisterUserUseCase;
import ncasa.identityaccess.infrastructure.security.IdentityUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
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

    public AuthController(RegisterUserUseCase registerUser, LoginUserUseCase loginUser,
            RefreshSessionUseCase refreshSession, LogoutUserUseCase logoutUser) {
        this.registerUser = registerUser;
        this.loginUser = loginUser;
        this.refreshSession = refreshSession;
        this.logoutUser = logoutUser;
    }

    @PostMapping("/register")
    @Transactional
    ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TokenResponse.from(registerUser.execute(request.email(), request.password())));
    }

    @PostMapping("/login")
    @Transactional
    TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.from(loginUser.execute(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    @Transactional
    TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return TokenResponse.from(refreshSession.execute(request.refreshToken()));
    }

    @GetMapping("/me")
    UserResponse me(@AuthenticationPrincipal IdentityUserDetails user) {
        return new UserResponse(user.id(), user.email(),
                user.getAuthorities().stream().map(Object::toString).sorted().toList());
    }

    @PostMapping("/logout")
    @Transactional
    ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request,
            @AuthenticationPrincipal IdentityUserDetails user) {
        logoutUser.execute(request.refreshToken(), user.id());
        return ResponseEntity.noContent().build();
    }

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 8, max = 128) String password) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record LogoutRequest(@NotBlank String refreshToken) {}
    public record UserResponse(Long id, String email, List<String> roles) {}

    public record TokenResponse(String accessToken, String refreshToken, String tokenType,
            long expiresIn, UserResponse user) {
        static TokenResponse from(AuthenticationResult result) {
            AuthenticatedUser user = result.user();
            return new TokenResponse(result.accessToken(), result.refreshToken(), result.tokenType(), result.expiresIn(),
                    new UserResponse(user.id(), user.email(), user.roles()));
        }
    }
}
