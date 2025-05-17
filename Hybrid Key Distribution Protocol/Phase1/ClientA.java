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
            // Connect to KDC
            Socket socket = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Request Key
            out.println("A");

            // Receive Encrypted KA and Private Key
            String encryptedKA = in.readLine();
            String privateKeyString = in.readLine();

            // Decode and reconstruct the Private Key
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Decrypt KA
            String KA = RSAUtil.decrypt(encryptedKA, privateKey);

            System.out.println("Client A Received:");
            System.out.println("Decrypted KA: " + KA);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


