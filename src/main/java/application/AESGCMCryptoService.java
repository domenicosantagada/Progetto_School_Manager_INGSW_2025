package application;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESGCMCryptoService {

    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;
    private static final int KEY_LEN_BITS = 256;
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int GCM_TAG_LENGTH = 128;

    private static final SecureRandom secureRandom = new SecureRandom();

    private static SecretKey deriveKey(char[] passphrase, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, KEY_LEN_BITS);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String plainText, String passphrase) throws Exception {
        byte[] salt = new byte[SALT_LEN];
        secureRandom.nextBytes(salt);

        SecretKey key = deriveKey(passphrase.toCharArray(), salt);

        byte[] iv = new byte[IV_LEN];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] cipherBytes = cipher.doFinal(plainBytes);

        byte[] out = new byte[salt.length + iv.length + cipherBytes.length];
        System.arraycopy(salt, 0, out, 0, salt.length);
        System.arraycopy(iv, 0, out, salt.length, iv.length);
        System.arraycopy(cipherBytes, 0, out, salt.length + iv.length, cipherBytes.length);

        return Base64.getEncoder().encodeToString(out);
    }

    public static String decrypt(String base64Input, String passphrase) throws Exception {
        byte[] all = Base64.getDecoder().decode(base64Input);

        if (all.length < SALT_LEN + IV_LEN + 1) {
            throw new IllegalArgumentException("Input cifrato non valido.");
        }

        byte[] salt = new byte[SALT_LEN];
        byte[] iv = new byte[IV_LEN];
        byte[] cipherBytes = new byte[all.length - SALT_LEN - IV_LEN];

        System.arraycopy(all, 0, salt, 0, SALT_LEN);
        System.arraycopy(all, SALT_LEN, iv, 0, IV_LEN);
        System.arraycopy(all, SALT_LEN + IV_LEN, cipherBytes, 0, cipherBytes.length);

        SecretKey key = deriveKey(passphrase.toCharArray(), salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] plainBytes = cipher.doFinal(cipherBytes);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    public static boolean verifyPassword(String candidatePlain, String storedEncryptedBase64, String passphrase) {
        try {
            String decrypted = decrypt(storedEncryptedBase64, passphrase);
            // confronto costante per mitigare timing attacks (semplice implementazione)
            return constantTimeEquals(decrypted.getBytes(StandardCharsets.UTF_8), candidatePlain.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}