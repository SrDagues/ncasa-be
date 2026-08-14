package ncasa.auth.dto;

import java.util.List;
import ncasa.security.CustomUserDetails;
import ncasa.user.entity.User;

public record UserResponse(Long id, String email, List<String> roles) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(),
                user.getRoles().stream().map(Enum::name).sorted().toList());
    }

    public static UserResponse from(CustomUserDetails user) {
        return new UserResponse(user.id(), user.email(),
                user.getAuthorities().stream().map(Object::toString).sorted().toList());
    }
}
