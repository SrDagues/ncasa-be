package ncasa.identityaccess.application;

import java.util.List;

public record AuthenticatedUser(Long id, String email, List<String> roles) {}
