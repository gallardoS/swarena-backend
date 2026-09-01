package dev.swami.arena.turnstile;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TurnstileVerifier {

    private static final URI SITEVERIFY_URI =
            URI.create("https://challenges.cloudflare.com/turnstile/v0/siteverify");
    private static final String SIGNUP_ACTION = "signup";

    private final TurnstileProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    TurnstileVerifier(
            TurnstileProperties properties,
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public void verifySignup(String token, String remoteAddress) {
        String secret = properties.secret();
        Set<String> allowedHostnames = normalizedHostnames(properties.hostnames());
        if (secret == null || secret.isBlank() || allowedHostnames.isEmpty()
                || token == null || token.isBlank() || token.length() > 2048) {
            throw new TurnstileVerificationException();
        }

        String form = formField("secret", secret)
                + "&" + formField("response", token);
        if (remoteAddress != null && !remoteAddress.isBlank()) {
            form += "&" + formField("remoteip", remoteAddress);
        }

        HttpRequest request = HttpRequest.newBuilder(SITEVERIFY_URI)
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() != 200) {
                throw new TurnstileVerificationException();
            }

            SiteverifyResponse result = objectMapper.readValue(response.body(), SiteverifyResponse.class);
            String hostname = result.hostname() == null
                    ? ""
                    : result.hostname().toLowerCase(Locale.ROOT);
            if (!result.success()
                    || !SIGNUP_ACTION.equals(result.action())
                    || !allowedHostnames.contains(hostname)) {
                throw new TurnstileVerificationException();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new TurnstileVerificationException(exception);
        } catch (IOException exception) {
            throw new TurnstileVerificationException(exception);
        }
    }

    private static String formField(String name, String value) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Set<String> normalizedHostnames(List<String> hostnames) {
        if (hostnames == null) {
            return Set.of();
        }
        return hostnames.stream()
                .filter(hostname -> hostname != null && !hostname.isBlank())
                .map(hostname -> hostname.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    private record SiteverifyResponse(
            boolean success,
            String hostname,
            String action,
            @JsonProperty("error-codes") List<String> errorCodes
    ) {
    }
}
