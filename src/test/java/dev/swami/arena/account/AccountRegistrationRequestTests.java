package dev.swami.arena.account;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRegistrationRequestTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidRegistration() {
        AccountRegistrationRequest request =
                new AccountRegistrationRequest("Swami123", "secret123", "swami@example.com", "token");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsValuesTheGameClientCannotUse() {
        AccountRegistrationRequest request =
                new AccountRegistrationRequest("invalid user", "this-password-is-too-long", "not-an-email", "token");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("username", "password", "email");
    }
}
