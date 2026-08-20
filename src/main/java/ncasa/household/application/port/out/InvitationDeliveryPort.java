package ncasa.household.application.port.out;

import ncasa.household.domain.HouseholdInvitation;

public interface InvitationDeliveryPort { void deliver(HouseholdInvitation invitation, String rawToken); }
