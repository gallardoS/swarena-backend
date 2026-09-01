package dev.swami.arena.turnstile;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("turnstile")
public record TurnstileProperties(
        String secret,
        List<String> hostnames
) {
}
