package dev.swami.arena.account;

import dev.swami.arena.config.RegistrationProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AccountRegistrationServiceTests {

    @Test
    void normalizesCredentialsAndUsesConfiguredExpansion() {
        AccountRepository repository = mock(AccountRepository.class);
        AzerothCoreSrp6 srp6 = mock(AzerothCoreSrp6.class);
        byte[] salt = new byte[32];
        byte[] verifier = new byte[32];
        when(srp6.createRegistrationData("SWAMI", "SECRET123"))
                .thenReturn(new AzerothCoreSrp6.RegistrationData(salt, verifier));
        AccountRegistrationService service = new AccountRegistrationService(
                repository,
                srp6,
                new RegistrationProperties(true, 2)
        );

        AccountRegistrationResponse response = service.register(
                new AccountRegistrationRequest("Swami", "secret123", "swami@example.com", "token")
        );

        ArgumentCaptor<String> email = ArgumentCaptor.forClass(String.class);
        verify(repository).create(eq("SWAMI"), email.capture(), eq(salt), eq(verifier), eq(2));
        assertThat(email.getValue()).isEqualTo("SWAMI@EXAMPLE.COM");
        assertThat(response.username()).isEqualTo("SWAMI");
    }

    @Test
    void reportsDuplicateUsername() {
        AccountRepository repository = mock(AccountRepository.class);
        AzerothCoreSrp6 srp6 = mock(AzerothCoreSrp6.class);
        when(srp6.createRegistrationData(anyString(), anyString()))
                .thenReturn(new AzerothCoreSrp6.RegistrationData(new byte[32], new byte[32]));
        doThrow(new DuplicateKeyException("duplicate"))
                .when(repository).create(anyString(), anyString(), any(byte[].class), any(byte[].class), anyInt());
        AccountRegistrationService service = new AccountRegistrationService(
                repository,
                srp6,
                new RegistrationProperties(true, 2)
        );

        assertThatThrownBy(() -> service.register(
                new AccountRegistrationRequest("Swami", "secret123", "swami@example.com", "token")
        )).isInstanceOf(AccountAlreadyExistsException.class);
    }

    @Test
    void doesNotGenerateCredentialsWhenRegistrationIsDisabled() {
        AccountRepository repository = mock(AccountRepository.class);
        AzerothCoreSrp6 srp6 = mock(AzerothCoreSrp6.class);
        AccountRegistrationService service = new AccountRegistrationService(
                repository,
                srp6,
                new RegistrationProperties(false, 2)
        );

        assertThatThrownBy(() -> service.register(
                new AccountRegistrationRequest("Swami", "secret123", "swami@example.com", "token")
        )).isInstanceOf(RegistrationDisabledException.class);
        verifyNoInteractions(repository, srp6);
    }
}
