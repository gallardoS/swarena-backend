package dev.swami.arena.turnstile;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TurnstileVerifierTests {

    @Test
    void acceptsSuccessfulSignupFromAllowedHostname() throws Exception {
        TurnstileVerifier verifier = verifierFor(
                "{\"success\":true,\"hostname\":\"localhost\",\"action\":\"signup\"}"
        );

        assertThatCode(() -> verifier.verifySignup("fresh-token", "127.0.0.1"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsUnexpectedAction() throws Exception {
        TurnstileVerifier verifier = verifierFor(
                "{\"success\":true,\"hostname\":\"localhost\",\"action\":\"login\"}"
        );

        assertThatThrownBy(() -> verifier.verifySignup("fresh-token", "127.0.0.1"))
                .isInstanceOf(TurnstileVerificationException.class);
    }

    @Test
    void rejectsUnexpectedHostname() throws Exception {
        TurnstileVerifier verifier = verifierFor(
                "{\"success\":true,\"hostname\":\"example.com\",\"action\":\"signup\"}"
        );

        assertThatThrownBy(() -> verifier.verifySignup("fresh-token", "127.0.0.1"))
                .isInstanceOf(TurnstileVerificationException.class);
    }

    @Test
    void failsClosedWhenSecretIsMissing() {
        TurnstileVerifier verifier = new TurnstileVerifier(
                new TurnstileProperties("", List.of("localhost")),
                mock(HttpClient.class),
                new ObjectMapper()
        );

        assertThatThrownBy(() -> verifier.verifySignup("fresh-token", "127.0.0.1"))
                .isInstanceOf(TurnstileVerificationException.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static TurnstileVerifier verifierFor(String responseBody) throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(responseBody);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        return new TurnstileVerifier(
                new TurnstileProperties("secret", List.of("localhost", "arena.swami.dev")),
                client,
                new ObjectMapper()
        );
    }
}
