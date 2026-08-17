package ncasa.identityaccess.infrastructure.security;

import ncasa.identityaccess.application.port.out.UserAccountRepository;
import ncasa.identityaccess.domain.Email;
import ncasa.identityaccess.domain.UserId;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class IdentityUserDetailsService implements UserDetailsService {
    private final UserAccountRepository users;

    public IdentityUserDetailsService(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return users.findByEmail(Email.of(email))
                .map(IdentityUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }

    public IdentityUserDetails loadUserById(Long id) {
        return users.findById(new UserId(id))
                .map(IdentityUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
