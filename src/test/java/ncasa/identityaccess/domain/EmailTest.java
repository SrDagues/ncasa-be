package ncasa.identityaccess.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailTest {
    @Test
    void shouldNormalizeEmail() {
        assertThat(Email.of(" User@Example.com ").value()).isEqualTo("user@example.com");
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> Email.of("not-an-email")).isInstanceOf(InvalidEmailException.class);
    }
}
