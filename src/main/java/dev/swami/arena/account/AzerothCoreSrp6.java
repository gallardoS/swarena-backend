package dev.swami.arena.account;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
class AzerothCoreSrp6 {

    private static final BigInteger GENERATOR = BigInteger.valueOf(7);
    private static final BigInteger LARGE_SAFE_PRIME = new BigInteger(
            "894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7",
            16
    );
    private static final int FIELD_SIZE = 32;

    private final SecureRandom secureRandom;

    AzerothCoreSrp6() {
        this(new SecureRandom());
    }

    AzerothCoreSrp6(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    RegistrationData createRegistrationData(String username, String password) {
        byte[] salt = new byte[FIELD_SIZE];
        secureRandom.nextBytes(salt);
        return createRegistrationData(username, password, salt);
    }

    RegistrationData createRegistrationData(String username, String password, byte[] salt) {
        if (salt.length != FIELD_SIZE) {
            throw new IllegalArgumentException("SRP6 salt must contain exactly 32 bytes");
        }

        byte[] credentialsHash = sha1((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        byte[] exponentHash = sha1(concatenate(salt, credentialsHash));
        BigInteger exponent = fromLittleEndian(exponentHash);
        BigInteger verifier = GENERATOR.modPow(exponent, LARGE_SAFE_PRIME);

        return new RegistrationData(salt.clone(), toLittleEndian(verifier, FIELD_SIZE));
    }

    private static byte[] sha1(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(input);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-1 is required by the AzerothCore SRP6 protocol", exception);
        }
    }

    private static byte[] concatenate(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static BigInteger fromLittleEndian(byte[] bytes) {
        return new BigInteger(1, reverse(bytes));
    }

    private static byte[] toLittleEndian(BigInteger value, int size) {
        byte[] bigEndian = value.toByteArray();
        int firstValueByte = bigEndian.length > 1 && bigEndian[0] == 0 ? 1 : 0;
        int valueLength = bigEndian.length - firstValueByte;
        if (valueLength > size) {
            throw new IllegalArgumentException("Value does not fit in the requested field size");
        }

        byte[] littleEndian = new byte[size];
        for (int index = 0; index < valueLength; index++) {
            littleEndian[index] = bigEndian[bigEndian.length - 1 - index];
        }
        return littleEndian;
    }

    private static byte[] reverse(byte[] bytes) {
        byte[] reversed = new byte[bytes.length];
        for (int index = 0; index < bytes.length; index++) {
            reversed[index] = bytes[bytes.length - 1 - index];
        }
        return reversed;
    }

    record RegistrationData(byte[] salt, byte[] verifier) {
    }
}
