package ncasa.identityaccess.infrastructure.security;

import ncasa.identityaccess.application.port.out.PasswordHasher;
import ncasa.identityaccess.domain.PasswordHash;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringPasswordHasher implements PasswordHasher {
    private final PasswordEncoder encoder;

    public SpringPasswordHasher(PasswordEncoder encoder) { this.encoder = encoder; }

    @Override public PasswordHash hash(String rawPassword) { return new PasswordHash(encoder.encode(rawPassword)); }
    @Override public boolean matches(String rawPassword, PasswordHash hash) { return encoder.matches(rawPassword, hash.value()); }
}
