package ncasa.identityaccess.infrastructure.security;

import java.util.Collection;
import ncasa.identityaccess.domain.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record IdentityUserDetails(Long id, String email, String password, boolean enabled,
        Collection<? extends GrantedAuthority> authorities) implements UserDetails {
    public static IdentityUserDetails from(UserAccount account) {
        return new IdentityUserDetails(account.id().value(), account.email().value(), account.passwordHash().value(),
                account.canAuthenticate(), account.roles().stream().map(Enum::name)
                        .map(SimpleGrantedAuthority::new).toList());
    }
    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isEnabled() { return enabled; }
}
