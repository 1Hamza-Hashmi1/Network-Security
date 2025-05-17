/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author hamza
 */
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Bob {
    public static void main(String[] args) {
        try {
            // Step 1: Create Server Socket
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Bob is waiting for Alice...\n");

            // Step 2: Accept Alice's Connection
            Socket socket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Step 3: Receive Message, Signature, and Public Key
            String receivedMessage = in.readLine();
            String receivedSignature = in.readLine();
            String receivedPublicKey = in.readLine();

            // Close connection
            socket.close();
            serverSocket.close();

            // Step 4: Extract Nonce
            String[] parts = receivedMessage.split("\\|");
            String message = parts[0];
            long nonce = Long.parseLong(parts[1]);

            // Step 5: Print what Bob received
            System.out.println("\n=== Bob ===");
            System.out.println("Received Message: " + message);
            System.out.println("Received Nonce: " + nonce);
            System.out.println("Received Signature: " + receivedSignature);
            System.out.println("Received Public Key: " + receivedPublicKey);

            // Step 6: Replay Attack Prevention - Check if nonce is fresh (within 5 min)
            long currentTime = System.currentTimeMillis();
            if (currentTime - nonce > 300000) { // 300000ms = 5 minutes
                System.out.println("\nReplay attack detected! Message is too old.");
                return;
            }

            // Step 7: Convert Public Key back to Java Object
            byte[] publicKeyBytes = Base64.getDecoder().decode(receivedPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            // Step 8: Verify Signature
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(receivedMessage.getBytes());
            boolean isValid = signature.verify(Base64.getDecoder().decode(receivedSignature));

            // Step 9: Print Result
            if (isValid) {
                System.out.println("\nSignature verified successfully! Message is authentic.");
            } else {
                System.out.println("\nInvalid signature! Message might be tampered with.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
