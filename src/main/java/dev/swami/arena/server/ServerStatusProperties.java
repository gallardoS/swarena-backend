package dev.swami.arena.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("swarena.server")
public record ServerStatusProperties(
        String authHost,
        int authPort,
        String worldHost,
        int worldPort,
        Duration connectTimeout,
        Duration cacheTtl
) {

    public ServerStatusProperties {
        requireHost(authHost, "auth-host");
        requirePort(authPort, "auth-port");
        requireHost(worldHost, "world-host");
        requirePort(worldPort, "world-port");
        requirePositive(connectTimeout, "connect-timeout");
        requirePositive(cacheTtl, "cache-ttl");
    }

    private static void requireHost(String host, String property) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("swarena.server." + property + " must not be blank");
        }
    }

    private static void requirePort(int port, String property) {
        if (port < 1 || port > 65_535) {
            throw new IllegalArgumentException("swarena.server." + property + " must be between 1 and 65535");
        }
    }

    private static void requirePositive(Duration duration, String property) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("swarena.server." + property + " must be positive");
        }
    }
}
