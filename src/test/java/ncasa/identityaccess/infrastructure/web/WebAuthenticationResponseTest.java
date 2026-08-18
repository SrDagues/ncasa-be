package ncasa.identityaccess.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import ncasa.identityaccess.application.AuthenticatedUser;
import ncasa.identityaccess.application.AuthenticationResult;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class WebAuthenticationResponseTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void shouldExposeAccessTokenAndUserWithoutRefreshToken() throws Exception {
        var result = new AuthenticationResult(
                "access-token",
                "refresh-token",
                "Bearer",
                900,
                new AuthenticatedUser(1L, "user@example.com", List.of("ROLE_USER")));

        String response = json.writeValueAsString(WebAuthenticationResponse.from(result));

        assertThat(response)
                .contains("\"accessToken\":\"access-token\"")
                .contains("\"tokenType\":\"Bearer\"")
                .contains("\"expiresIn\":900")
                .contains("\"email\":\"user@example.com\"")
                .doesNotContain("refreshToken")
                .doesNotContain("refresh-token");
    }
}
