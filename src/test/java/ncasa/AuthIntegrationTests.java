package ncasa;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import jakarta.servlet.http.Cookie;
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
import org.springframework.test.web.servlet.MvcResult;
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
        jdbc.update("DELETE FROM household_invitations");
        jdbc.update("DELETE FROM household_members");
        jdbc.update("DELETE FROM households");
        jdbc.update("DELETE FROM refresh_tokens");
        jdbc.update("DELETE FROM auth_identities");
        jdbc.update("DELETE FROM user_roles");
        jdbc.update("DELETE FROM users");
    }

    @Test void shouldRegisterUserWhenDataIsValid() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"User@Example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("ncasa_refresh="),
                        org.hamcrest.Matchers.containsString("HttpOnly"))))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
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
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("ncasa_refresh="),
                        org.hamcrest.Matchers.containsString("HttpOnly"))))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
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
        register("user@example.com", "password123");
        Cookie initial = loginRefreshCookie("user@example.com", "password123");

        mvc.perform(post("/api/auth/refresh").cookie(initial))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("ncasa_refresh=")))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test void shouldReturnUnauthorizedWhenRefreshCookieIsMissing() throws Exception {
        mvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test void shouldReturnUnauthorizedWhenRefreshCookieIsInvalid() throws Exception {
        mvc.perform(post("/api/auth/refresh").cookie(new Cookie("ncasa_refresh", "invalid-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test void shouldRejectExpiredRefreshToken() throws Exception {
        register("user@example.com", "password123");
        Cookie refresh = loginRefreshCookie("user@example.com", "password123");
        jdbc.update("UPDATE refresh_tokens SET expires_at = ? WHERE token_hash = ?",
                java.sql.Timestamp.from(Instant.now().minusSeconds(1)), sha256(refresh.getValue()));
        refreshExpectUnauthorized(refresh);
    }

    @Test void shouldRejectRevokedRefreshToken() throws Exception {
        register("user@example.com", "password123");
        Cookie refresh = loginRefreshCookie("user@example.com", "password123");

        mvc.perform(post("/api/auth/logout").cookie(refresh))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("ncasa_refresh="),
                        org.hamcrest.Matchers.containsString("Max-Age=0"))));

        mvc.perform(post("/api/auth/refresh").cookie(refresh))
                .andExpect(status().isUnauthorized());
    }

    @Test void shouldLogoutIdempotentlyWhenRefreshCookieIsMissing() throws Exception {
        mvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
    }

    @Test void shouldRevokePreviousRefreshTokenAfterRotation() throws Exception {
        register("user@example.com", "password123");
        Cookie cookieA = loginRefreshCookie("user@example.com", "password123");

        String rotatedHeader = mvc.perform(post("/api/auth/refresh").cookie(cookieA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader("Set-Cookie");
        Cookie cookieB = refreshCookieFrom(rotatedHeader);

        org.assertj.core.api.Assertions.assertThat(cookieB.getValue()).isNotEqualTo(cookieA.getValue());
        mvc.perform(post("/api/auth/refresh").cookie(cookieA)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").cookie(cookieB)).andExpect(status().isOk());
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

    @Test void shouldAllowCredentialsForConfiguredCorsOrigin() throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test void shouldRejectUnknownCorsOrigin() throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header("Origin", "https://attacker.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test void shouldCompleteWebAuthenticationLifecycle() throws Exception {
        String credentials = json.writeValueAsString(
                java.util.Map.of("email", "user@example.com", "password", "password123"));

        MvcResult registration = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isCreated()).andReturn();
        JsonNode registrationBody = json.readTree(registration.getResponse().getContentAsString());
        Cookie cookieA = refreshCookieFrom(registration.getResponse().getHeader("Set-Cookie"));

        mvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + registrationBody.get("accessToken").asString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));

        MvcResult refresh = mvc.perform(post("/api/auth/refresh").cookie(cookieA))
                .andExpect(status().isOk()).andReturn();
        JsonNode refreshBody = json.readTree(refresh.getResponse().getContentAsString());
        Cookie cookieB = refreshCookieFrom(refresh.getResponse().getHeader("Set-Cookie"));

        mvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + refreshBody.get("accessToken").asString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));

        mvc.perform(post("/api/auth/logout").cookie(cookieB))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/refresh").cookie(cookieB))
                .andExpect(status().isUnauthorized());
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

    private Cookie loginRefreshCookie(String email, String password) throws Exception {
        String body = json.writeValueAsString(java.util.Map.of("email", email, "password", password));
        String setCookie = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getHeader("Set-Cookie");
        return refreshCookieFrom(setCookie);
    }

    private Cookie refreshCookieFrom(String setCookie) {
        String pair = setCookie.substring(0, setCookie.indexOf(';'));
        return new Cookie(pair.substring(0, pair.indexOf('=')), pair.substring(pair.indexOf('=') + 1));
    }

    private void refreshExpectUnauthorized(Cookie refreshCookie) throws Exception {
        mvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isUnauthorized());
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
