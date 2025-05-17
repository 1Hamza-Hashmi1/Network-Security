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
import java.util.Base64;

public class Alice {
    public static void main(String[] args) {
        try {
            // Step 1: Generate RSA Key Pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // Step 2: Prepare Message and Signature
            String message = "Hello Bob, this is Alice!";
            long nonce = System.currentTimeMillis(); // Use fresh nonce
            String messageWithNonce = message + "|" + nonce;

            // Step 3: Sign the message with Alice's private key
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(messageWithNonce.getBytes());
            byte[] signedMessage = signature.sign();

            // Encode for transmission
            String encodedSignature = Base64.getEncoder().encodeToString(signedMessage);
            String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            // Print what Alice is sending
            System.out.println("\n=== Alice ===");
            System.out.println("Message: " + message);
            System.out.println("Nonce: " + nonce);
            System.out.println("Signature: " + encodedSignature);
            System.out.println("Public Key: " + encodedPublicKey);
            System.out.println("Sending to Bob...\n");

            // Step 4: Connect to Bob's Server
            Socket socket = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Step 5: Send Message, Signature, and Public Key
            out.println(messageWithNonce);
            out.println(encodedSignature);
            out.println(encodedPublicKey);

            // Close connection
            socket.close();
            System.out.println("Message, Signature, and Public Key sent to Bob.\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

