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
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class ClientA {
    public static void main(String[] args) {
        try {
            // Connect to KDC for Master Key
            Socket socket = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Request Master Key KA
            out.println("A");
            String encryptedKA = in.readLine();
            String privateKeyString = in.readLine();
            socket.close(); // Close the connection to KDC

            // Decode and reconstruct Private Key
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Decrypt KA
            String KA = RSAUtil.decrypt(encryptedKA, privateKey);
            System.out.println("Client A Received KA: " + KA);

            // Step 1: Request Session Key
            Socket sessionSocket = new Socket("localhost", 5000);
            PrintWriter sessionOut = new PrintWriter(sessionSocket.getOutputStream(), true);
            BufferedReader sessionIn = new BufferedReader(new InputStreamReader(sessionSocket.getInputStream()));

            sessionOut.println("SESSION_REQUEST"); // Request session key from KDC
            String encryptedKABForAlice = sessionIn.readLine();
            sessionSocket.close(); // Close after receiving session key

            // Decrypt session key for Alice
            String KAB = RSAUtil.decrypt(encryptedKABForAlice, privateKey);
            System.out.println("Client A Decrypted Session Key: " + KAB);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
