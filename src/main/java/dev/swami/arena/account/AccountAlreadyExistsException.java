package dev.swami.arena.account;

public class AccountAlreadyExistsException extends RuntimeException {

    AccountAlreadyExistsException() {
        super("An account with that username already exists");
    }
}
