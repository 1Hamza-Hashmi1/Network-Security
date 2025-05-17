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

public class ClientB {
    public static void main(String[] args) {
        try {
            // Connect to KDC
            Socket socket = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Request Key
            out.println("B");

            // Receive Encrypted KB and Private Key
            String encryptedKB = in.readLine();
            String privateKeyString = in.readLine();

            // Debug Print: Show Encrypted KB
            System.out.println("===== Client B Debugging =====");
            System.out.println("Received Encrypted KB: " + encryptedKB);
            System.out.println("Received Private Key (Base64): " + privateKeyString);

            // Decode and reconstruct the Private Key
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Debug Print: Show Decoded Private Key
            System.out.println("Reconstructed Private Key B: " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));

            // Decrypt KB
            String KB = RSAUtil.decrypt(encryptedKB, privateKey);

            // Debug Print: Show Decrypted KB
            System.out.println("Decrypted KB: " + KB);
            System.out.println("=============================");

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
