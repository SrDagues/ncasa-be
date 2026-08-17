package ncasa.identityaccess.application.port.out;

import ncasa.identityaccess.domain.UserAccount;

public interface AccessTokenIssuer {
    IssuedAccessToken issue(UserAccount account);

    record IssuedAccessToken(String value, long expiresInSeconds) {}
}
