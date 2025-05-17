/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author hamza
 */
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Base64;

public class AliceClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to Bob (Server)");

            // Generate Nonce_A
            SecureRandom random = new SecureRandom();
            byte[] nonceA = new byte[16];
            random.nextBytes(nonceA);
            String nonceAString = Base64.getEncoder().encodeToString(nonceA);

            // Step 1: Alice sends ID_A and Nonce_A to Bob
            String ID_A = "Alice";
            out.println(ID_A + "," + nonceAString);
            System.out.println("Sent: ID_A=" + ID_A + ", Nonce_A=" + nonceAString);

            // Step 2: Receive Nonce_B and Encrypted (ID_B, Nonce_A)
            String response = in.readLine();
            String[] parts = response.split(",");
            String nonceB = parts[0];
            String encryptedMessage = parts[1];

            System.out.println("Received: Nonce_B=" + nonceB + ", Encrypted(ID_B, Nonce_A)=" + encryptedMessage);

            // Step 3: Decrypt (ID_B, Nonce_A)
            SecretKey sharedKey = AESUtil.getFixedAESKey();  // Both Alice and Bob have the same shared key
            String decryptedMessage = AESUtil.decrypt(encryptedMessage, sharedKey);
            String[] decryptedParts = decryptedMessage.split(",");
            String receivedID_B = decryptedParts[0];
            String receivedNonceA = decryptedParts[1];

            System.out.println("Decrypted: ID_B=" + receivedID_B + ", Nonce_A=" + receivedNonceA);

            // Verify that received Nonce_A matches the original Nonce_A
            if (!receivedNonceA.equals(nonceAString)) {
                System.out.println("Error: Nonce_A verification failed!");
                return;
            }

            // Step 4: Send Encrypted (ID_A, Nonce_B) to Bob
            String messageToBob = ID_A + "," + nonceB;
            String encryptedMessageToBob = AESUtil.encrypt(messageToBob, sharedKey);
            out.println(encryptedMessageToBob);
            System.out.println("Sent Encrypted (ID_A, Nonce_B): " + encryptedMessageToBob);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

