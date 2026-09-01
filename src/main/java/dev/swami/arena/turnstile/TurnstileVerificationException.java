package dev.swami.arena.turnstile;

public class TurnstileVerificationException extends RuntimeException {

    TurnstileVerificationException() {
        super("Turnstile verification failed");
    }

    TurnstileVerificationException(Throwable cause) {
        super("Turnstile verification failed", cause);
    }
}
