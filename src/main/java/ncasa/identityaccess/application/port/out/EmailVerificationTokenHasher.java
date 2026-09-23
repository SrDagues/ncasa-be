package ncasa.identityaccess.application.port.out;

import ncasa.identityaccess.domain.EmailVerificationTokenHash;

public interface EmailVerificationTokenHasher {
    EmailVerificationTokenHash hash(String rawToken);
}
