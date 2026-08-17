package ncasa.identityaccess.application.port.out;

import ncasa.identityaccess.domain.PasswordHash;

public interface PasswordHasher {
    PasswordHash hash(String rawPassword);
    boolean matches(String rawPassword, PasswordHash hash);
}
