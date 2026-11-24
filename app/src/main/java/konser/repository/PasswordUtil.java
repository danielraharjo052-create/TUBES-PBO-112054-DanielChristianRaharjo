package konser.repository;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;

import java.util.HexFormat;

public final class PasswordUtil {

    private PasswordUtil() {
    }

public static String sha256(String value) {

    try {

        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(digest);

    }
catch (NoSuchAlgorithmException e) {
    throw new IllegalStateException("SHA-256 tidak tersedia", e);
}

}

}
