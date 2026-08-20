package ncasa.household.application.port.out;

import ncasa.household.domain.InvitationTokenHash;

public interface InvitationTokenHasher { InvitationTokenHash hash(String rawToken); }
