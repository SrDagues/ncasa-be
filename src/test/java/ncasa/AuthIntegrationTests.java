package ncasa;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class AuthIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JwtDecoder decoder;
    @Autowired JwtEncoder encoder;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM refresh_tokens");
        jdbc.update("DELETE FROM auth_identities");
        jdbc.update("DELETE FROM user_roles");
        jdbc.update("DELETE FROM users");
    }

    @Test void shouldRegisterUserWhenDataIsValid() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"User@Example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.user.roles[0]").value("ROLE_USER"));
    }

    @Test void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        register("user@example.com", "password123");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"USER@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict());
    }

    @Test void shouldRejectInvalidEmail() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.email").exists());
    }

    @Test void shouldLoginWhenCredentialsAreValid() throws Exception {
        register("user@example.com", "password123");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test void shouldReturnUnauthorizedWhenPasswordIsInvalid() throws Exception {
        register("user@example.com", "password123");
        loginExpectUnauthorized("user@example.com", "wrong-password");
    }

    @Test void shouldReturnUnauthorizedWhenUserDoesNotExist() throws Exception {
        loginExpectUnauthorized("missing@example.com", "password123");
    }

    @Test void shouldGenerateValidAccessToken() throws Exception {
        JsonNode tokens = register("user@example.com", "password123");
        var jwt = decoder.decode(tokens.get("accessToken").asString());
        org.assertj.core.api.Assertions.assertThat(jwt.getSubject()).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(jwt.getClaimAsString("email")).isEqualTo("user@example.com");
        org.assertj.core.api.Assertions.assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ROLE_USER");
    }

    @Test void shouldRejectExpiredToken() {
        String token = encodeWith(encoder, Instant.now().minusSeconds(120), Instant.now().minusSeconds(60));
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test void shouldRejectInvalidSignature() {
        var otherKey = new SecretKeySpec("a-different-secret-that-is-more-than-32-characters".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder otherEncoder = NimbusJwtEncoder.withSecretKey(otherKey).build();
        String token = encodeWith(otherEncoder, Instant.now(), Instant.now().plusSeconds(60));
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test void shouldRefreshTokensWhenRefreshTokenIsValid() throws Exception {
        JsonNode initial = register("user@example.com", "password123");
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(initial.get("refreshToken").asString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test void shouldRejectExpiredRefreshToken() throws Exception {
        JsonNode initial = register("user@example.com", "password123");
        String raw = initial.get("refreshToken").asString();
        jdbc.update("UPDATE refresh_tokens SET expires_at = ? WHERE token_hash = ?",
                java.sql.Timestamp.from(Instant.now().minusSeconds(1)), sha256(raw));
        refreshExpectUnauthorized(raw);
    }

    @Test void shouldRejectRevokedRefreshToken() throws Exception {
        JsonNode initial = register("user@example.com", "password123");
        String refresh = initial.get("refreshToken").asString();
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + initial.get("accessToken").asString())
                        .contentType(MediaType.APPLICATION_JSON).content(refreshBody(refresh)))
                .andExpect(status().isNoContent());
        refreshExpectUnauthorized(refresh);
    }

    @Test void shouldRevokePreviousRefreshTokenAfterRotation() throws Exception {
        JsonNode initial = register("user@example.com", "password123");
        String oldRefresh = initial.get("refreshToken").asString();
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshBody(oldRefresh)))
                .andExpect(status().isOk());
        refreshExpectUnauthorized(oldRefresh);
    }

    @Test void shouldAllowPublicEndpointWithoutAuthentication() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"public@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated());
    }

    @Test void shouldRejectProtectedEndpointWithoutJwt() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test void shouldAllowProtectedEndpointWithValidJwt() throws Exception {
        JsonNode tokens = register("user@example.com", "password123");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tokens.get("accessToken").asString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("user@example.com"));
    }

    private JsonNode register(String email, String password) throws Exception {
        String body = json.writeValueAsString(java.util.Map.of("email", email, "password", password));
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }

    private void loginExpectUnauthorized(String email, String password) throws Exception {
        String body = json.writeValueAsString(java.util.Map.of("email", email, "password", password));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    private void refreshExpectUnauthorized(String token) throws Exception {
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshBody(token)))
                .andExpect(status().isUnauthorized());
    }

    private String refreshBody(String token) throws Exception {
        return json.writeValueAsString(java.util.Map.of("refreshToken", token));
    }

    private String encodeWith(JwtEncoder jwtEncoder, Instant issued, Instant expires) {
        JwtClaimsSet claims = JwtClaimsSet.builder().subject("1").issuedAt(issued).expiresAt(expires)
                .claim("userId", 1L).claim("email", "user@example.com").claim("roles", java.util.List.of("ROLE_USER")).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
