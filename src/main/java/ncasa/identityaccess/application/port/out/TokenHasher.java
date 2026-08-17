package ncasa.identityaccess.application.port.out;

import ncasa.identityaccess.domain.RefreshTokenHash;

public interface TokenHasher {
    RefreshTokenHash hash(String rawToken);
}
