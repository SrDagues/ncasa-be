package ncasa.security;

import ncasa.user.entity.AuthProvider;
import ncasa.user.repository.AuthIdentityRepository;
import ncasa.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AuthIdentityRepository identities;
    private final UserRepository users;

    public CustomUserDetailsService(AuthIdentityRepository identities, UserRepository users) {
        this.identities = identities;
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return identities.findByProviderAndProviderUserId(AuthProvider.LOCAL, email.toLowerCase())
                .map(identity -> CustomUserDetails.from(identity.getUser(), identity.getPasswordHash()))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }

    public CustomUserDetails loadUserById(Long id) {
        return users.findById(id)
                .map(user -> CustomUserDetails.from(user, ""))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
