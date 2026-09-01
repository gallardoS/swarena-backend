package dev.swami.arena.account;

import dev.swami.arena.turnstile.TurnstileVerifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
class AccountRegistrationController {

    private final AccountRegistrationService registrationService;
    private final TurnstileVerifier turnstileVerifier;

    AccountRegistrationController(
            AccountRegistrationService registrationService,
            TurnstileVerifier turnstileVerifier
    ) {
        this.registrationService = registrationService;
        this.turnstileVerifier = turnstileVerifier;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    AccountRegistrationResponse register(
            @Valid @RequestBody AccountRegistrationRequest request,
            HttpServletRequest httpRequest
    ) {
        turnstileVerifier.verifySignup(request.turnstileToken(), httpRequest.getRemoteAddr());
        return registrationService.register(request);
    }
}
