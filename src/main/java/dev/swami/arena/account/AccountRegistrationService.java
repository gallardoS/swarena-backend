package dev.swami.arena.account;

import dev.swami.arena.config.RegistrationProperties;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
class AccountRegistrationService {

    private final AccountRepository accountRepository;
    private final AzerothCoreSrp6 srp6;
    private final RegistrationProperties properties;

    AccountRegistrationService(
            AccountRepository accountRepository,
            AzerothCoreSrp6 srp6,
            RegistrationProperties properties
    ) {
        this.accountRepository = accountRepository;
        this.srp6 = srp6;
        this.properties = properties;
    }

    @Transactional
    AccountRegistrationResponse register(AccountRegistrationRequest request) {
        if (!properties.enabled()) {
            throw new RegistrationDisabledException();
        }

        String username = request.username().toUpperCase(Locale.ROOT);
        String password = request.password().toUpperCase(Locale.ROOT);
        String email = request.email().toUpperCase(Locale.ROOT);
        AzerothCoreSrp6.RegistrationData registrationData =
                srp6.createRegistrationData(username, password);

        try {
            accountRepository.create(
                    username,
                    email,
                    registrationData.salt(),
                    registrationData.verifier(),
                    properties.expansion()
            );
        } catch (DuplicateKeyException exception) {
            throw new AccountAlreadyExistsException();
        }

        return new AccountRegistrationResponse(username);
    }
}
