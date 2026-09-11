package ncasa.household.application.port.out;
import java.util.UUID;
public interface InactiveMemberCleanupPort{void memberBecameInactive(UUID householdId,UUID memberId);InactiveMemberCleanupPort NONE=(h,m)->{};}
