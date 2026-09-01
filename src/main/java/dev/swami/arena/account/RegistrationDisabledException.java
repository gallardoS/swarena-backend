package dev.swami.arena.account;

public class RegistrationDisabledException extends RuntimeException {

    RegistrationDisabledException() {
        super("Account registration is currently disabled");
    }
}
