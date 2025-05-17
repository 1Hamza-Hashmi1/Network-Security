/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author hamza
 */
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class CryptoUtils {
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        return generator.generateKey();
    }

    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    public static String decrypt(String encrypted, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] bytes = Base64.getDecoder().decode(encrypted);
        byte[] decrypted = cipher.doFinal(bytes);
        return new String(decrypted);
    }

    public static SecretKey getKeyFromString(String keyString) {
        byte[] decoded = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decoded, 0, decoded.length, "AES");
    }

    public static String keyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static SecretKey[] deriveKeys(SecretKey masterSecret) throws Exception {
        byte[] secretBytes = masterSecret.getEncoded();
        Mac hmac = Mac.getInstance("HmacSHA256");

        hmac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
        byte[] encKeyBytes = hmac.doFinal("encryption".getBytes());
        SecretKey encKey = new SecretKeySpec(encKeyBytes, 0, 16, "AES");

        hmac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
        byte[] macKeyBytes = hmac.doFinal("mac".getBytes());
        SecretKey macKey = new SecretKeySpec(macKeyBytes, 0, 16, "AES");

        return new SecretKey[]{encKey, macKey};
    }

    public static String generateHMAC(String message, SecretKey macKey) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(macKey);
        byte[] macBytes = hmac.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(macBytes);
    }

    public static boolean verifyHMAC(String message, String receivedHmac, SecretKey macKey) throws Exception {
        String calculated = generateHMAC(message, macKey);
        return calculated.equals(receivedHmac);
    }
    
    public static String computeHMAC(String message, SecretKey key) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(key);
    byte[] hmac = mac.doFinal(message.getBytes());
    return Base64.getEncoder().encodeToString(hmac);
}
}
