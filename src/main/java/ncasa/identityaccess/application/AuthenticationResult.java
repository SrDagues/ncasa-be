package ncasa.identityaccess.application;

public record AuthenticationResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        AuthenticatedUser user) {}
