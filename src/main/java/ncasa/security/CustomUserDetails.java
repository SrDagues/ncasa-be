package ncasa.security;

import java.util.Collection;
import ncasa.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record CustomUserDetails(
        Long id,
        String email,
        String password,
        boolean enabled,
        Collection<? extends GrantedAuthority> authorities) implements UserDetails {

    public static CustomUserDetails from(User user, String passwordHash) {
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();
        return new CustomUserDetails(user.getId(), user.getEmail(), passwordHash, user.isEnabled(), authorities);
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
