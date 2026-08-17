package ncasa.auth.controller;

import jakarta.validation.Valid;
import ncasa.auth.dto.LoginRequest;
import ncasa.auth.dto.LogoutRequest;
import ncasa.auth.dto.RefreshRequest;
import ncasa.auth.dto.RegisterRequest;
import ncasa.auth.dto.TokenResponse;
import ncasa.auth.dto.UserResponse;
import ncasa.identityaccess.application.login.LoginUserUseCase;
import ncasa.identityaccess.application.logout.LogoutUserUseCase;
import ncasa.identityaccess.application.refresh.RefreshSessionUseCase;
import ncasa.identityaccess.application.register.RegisterUserUseCase;
import ncasa.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerUser.execute(request));
    }

    @PostMapping("/login")
    TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return loginUser.execute(request);
    }

    @PostMapping("/refresh")
    TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return refreshSession.execute(request.refreshToken());
    }

    @GetMapping("/me")
    UserResponse me(@AuthenticationPrincipal CustomUserDetails user) {
        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        logoutUser.execute(request.refreshToken(), user.id());
        return ResponseEntity.noContent().build();
    }
}
