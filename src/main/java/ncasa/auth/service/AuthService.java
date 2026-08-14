package ncasa.auth.service;

import java.util.Locale;
import ncasa.auth.dto.LoginRequest;
import ncasa.auth.dto.RegisterRequest;
import ncasa.auth.dto.TokenResponse;
import ncasa.auth.dto.UserResponse;
import ncasa.common.exception.EmailAlreadyExistsException;
import ncasa.security.CustomUserDetails;
import ncasa.security.JwtService;
import ncasa.user.entity.AuthIdentity;
import ncasa.user.entity.AuthProvider;
import ncasa.user.entity.User;
import ncasa.user.repository.AuthIdentityRepository;
import ncasa.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final AuthIdentityRepository identities;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokens;

    public AuthService(UserRepository users, AuthIdentityRepository identities, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService, RefreshTokenService refreshTokens) {
        this.users = users;
        this.identities = identities;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokens = refreshTokens;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) throw new EmailAlreadyExistsException();
        try {
            User user = users.save(new User(email));
            identities.save(new AuthIdentity(user, AuthProvider.LOCAL, email,
                    passwordEncoder.encode(request.password())));
            return issue(CustomUserDetails.from(user, ""), user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException();
        }
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        normalizeEmail(request.email()), request.password()));
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = users.getReferenceById(principal.id());
        return issue(principal, user);
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        var rotation = refreshTokens.rotate(rawRefreshToken);
        CustomUserDetails principal = CustomUserDetails.from(rotation.user(), "");
        return tokenResponse(principal, rotation.refreshToken());
    }

    @Transactional
    public void logout(String rawRefreshToken, Long userId) {
        refreshTokens.revoke(rawRefreshToken, userId);
    }

    private TokenResponse issue(CustomUserDetails principal, User user) {
        var refresh = refreshTokens.create(user);
        return tokenResponse(principal, refresh.value());
    }

    private TokenResponse tokenResponse(CustomUserDetails principal, String refreshToken) {
        return new TokenResponse(jwtService.createAccessToken(principal), refreshToken, "Bearer",
                jwtService.accessTokenExpiresInSeconds(), UserResponse.from(principal));
    }

    private String normalizeEmail(String email) { return email.strip().toLowerCase(Locale.ROOT); }
}
