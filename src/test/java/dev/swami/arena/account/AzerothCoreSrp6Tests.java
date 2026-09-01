package dev.swami.arena.account;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AzerothCoreSrp6Tests {

    private static final HexFormat HEX = HexFormat.of();

    @Test
    void createsAzerothCoreCompatibleVerifier() {
        byte[] salt = HEX.parseHex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");
        AzerothCoreSrp6 srp6 = new AzerothCoreSrp6(new SecureRandom());

        AzerothCoreSrp6.RegistrationData result =
                srp6.createRegistrationData("SWAMI", "SECRET123", salt);

        assertThat(result.salt()).containsExactly(salt);
        assertThat(HEX.formatHex(result.verifier()))
                .isEqualTo("21521b520004e7efe783226518e2d9fee3d23727e5e934aa808792b39dc52b5c");
    }

    @Test
    void rejectsSaltWithWrongLength() {
        AzerothCoreSrp6 srp6 = new AzerothCoreSrp6(new SecureRandom());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> srp6.createRegistrationData("SWAMI", "SECRET123", new byte[31]));
    }
}
