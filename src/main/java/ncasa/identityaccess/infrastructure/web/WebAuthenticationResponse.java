package ncasa.identityaccess.infrastructure.web;

import java.util.List;
import ncasa.identityaccess.application.AuthenticatedUser;
import ncasa.identityaccess.application.AuthenticationResult;

public record WebAuthenticationResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user) {

    static WebAuthenticationResponse from(AuthenticationResult result) {
        AuthenticatedUser user = result.user();
        return new WebAuthenticationResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresIn(),
                new UserResponse(user.id(), user.email(), user.roles()));
    }

    public record UserResponse(Long id, String email, List<String> roles) {}
}
