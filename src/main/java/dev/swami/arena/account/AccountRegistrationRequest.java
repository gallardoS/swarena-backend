package dev.swami.arena.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountRegistrationRequest(
        @NotBlank
        @Size(min = 3, max = 17)
        @Pattern(regexp = "[A-Za-z0-9]+", message = "must contain only letters and numbers")
        String username,

        @NotBlank
        @Size(min = 8, max = 16)
        @Pattern(regexp = "[\\x21-\\x7E]+", message = "must contain only printable ASCII characters and no spaces")
        String password,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(max = 2048)
        String turnstileToken
) {
}
