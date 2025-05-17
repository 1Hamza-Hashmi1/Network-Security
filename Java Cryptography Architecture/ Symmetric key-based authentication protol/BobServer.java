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
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Base64;

public class BobServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Bob (Server) is running... Waiting for Alice (Client) to connect.");

            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                System.out.println("Alice (Client) connected.");

                // Step 1: Receive ID_A and Nonce_A
                String request = in.readLine();
                String[] parts = request.split(",");
                String ID_A = parts[0];
                String nonceA = parts[1];

                System.out.println("Received: ID_A=" + ID_A + ", Nonce_A=" + nonceA);

                // Generate Nonce_B
                SecureRandom random = new SecureRandom();
                byte[] nonceB = new byte[16];
                random.nextBytes(nonceB);
                String nonceBString = Base64.getEncoder().encodeToString(nonceB);

                // Step 2: Send Nonce_B and Encrypted (ID_B, Nonce_A) to Alice
                String ID_B = "Bob";
                SecretKey sharedKey = AESUtil.getFixedAESKey();  // Both Alice and Bob have the same shared key
                String messageToAlice = ID_B + "," + nonceA;
                String encryptedMessage = AESUtil.encrypt(messageToAlice, sharedKey);

                out.println(nonceBString + "," + encryptedMessage);
                System.out.println("Sent: Nonce_B=" + nonceBString + ", Encrypted(ID_B, Nonce_A)=" + encryptedMessage);

                // Step 3: Receive Encrypted (ID_A, Nonce_B)
                String encryptedResponse = in.readLine();
                String decryptedResponse = AESUtil.decrypt(encryptedResponse, sharedKey);
                String[] responseParts = decryptedResponse.split(",");
                String receivedID_A = responseParts[0];
                String receivedNonceB = responseParts[1];

                System.out.println("Decrypted: ID_A=" + receivedID_A + ", Nonce_B=" + receivedNonceB);

                // Verify Nonce_B
                if (!receivedNonceB.equals(nonceBString)) {
                    System.out.println("Error: Nonce_B verification failed!");
                    return;
                }

                System.out.println("Authentication successful!");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

