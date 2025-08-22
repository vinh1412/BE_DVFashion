/*
 * @ {#} AESUtils.java   1.0     23/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/*
 * @description: Utility class for AES encryption and decryption
 * @author: Tran Hien Vinh
 * @date:   23/08/2025
 * @version:    1.0
 */
public class AESUtils {
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
     * Encrypts the given plain text using AES encryption.
     *
     * @param plainText The text to encrypt
     * @return The encrypted text in Base64 format
     */
    public String encrypt(String plainText) {
        try {
            // Create a Cipher instance for AES encryption
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            // Initialize the cipher with the secret key in encryption mode
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Encrypt the plain text
            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            // Encode the encrypted byte array to Base64 string for easy transport/storage
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    /**
     * Decrypts the given cipher text using AES decryption.
     *
     * @param cipherText The Base64 encoded text to decrypt
     * @return The decrypted plain text
     */
    public String decrypt(String cipherText) {
        try {
            // Create a Cipher instance for AES decryption
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            // Initialize the cipher with the secret key in decryption mode
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Decode the Base64 encoded cipher text and decrypt it
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));

            // Convert the decrypted byte array back to a string
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}
