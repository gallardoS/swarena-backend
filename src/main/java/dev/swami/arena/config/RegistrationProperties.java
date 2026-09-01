package dev.swami.arena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "swarena.registration")
public record RegistrationProperties(boolean enabled, int expansion) {

    public RegistrationProperties {
        if (expansion < 0 || expansion > 2) {
            throw new IllegalArgumentException("swarena.registration.expansion must be between 0 and 2");
        }
    }
}
