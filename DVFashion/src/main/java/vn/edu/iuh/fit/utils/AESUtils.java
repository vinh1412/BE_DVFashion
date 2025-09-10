/*
 * @ {#} AESUtils.java   2.0     10/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/*
 * @description: Secure utility class for AES encryption and decryption (using AES/GCM/NoPadding)
 * @author: Tran Hien Vinh (updated by ChatGPT)
 * @date:   10/09/2025
 * @version:    2.0
 */
public final class AESUtils {
    private static final String AES_ALGO = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;  // Recommended: 12 bytes
    private static final int GCM_TAG_LENGTH = 128; // Authentication tag length (bits)

    private final SecretKey secretKey;

    /**
     * Constructor to initialize AESUtils with a Base64 encoded key.
     *
     * @param base64Key Base64 encoded AES key
     */
    public AESUtils(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    /**
     * Encrypts the given plain text using AES-GCM encryption.
     *
     * @param plainText The text to encrypt
     * @return The encrypted text in Base64 format (IV + Ciphertext)
     */
    public String encrypt(String plainText) {
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(plainText.getBytes());

            // Prepend IV to ciphertext for later decryption
            byte[] encryptedWithIv = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedWithIv, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    /**
     * Decrypts the given cipher text using AES-GCM decryption.
     *
     * @param cipherText The Base64 encoded text to decrypt (IV + Ciphertext)
     * @return The decrypted plain text
     */
    public String decrypt(String cipherText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            // Extract IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            // Extract ciphertext
            byte[] ciphertext = new byte[decoded.length - iv.length];
            System.arraycopy(decoded, iv.length, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(AES_ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] decrypted = cipher.doFinal(ciphertext);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}
