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
            // Connect to KDC to receive Bob's Master Key (KB)
            Socket kdcSocket = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(kdcSocket.getOutputStream(), true);
            BufferedReader kdcIn = new BufferedReader(new InputStreamReader(kdcSocket.getInputStream()));

            out.println("B"); // Request Master Key KB
            String encryptedKB = kdcIn.readLine(); // Encrypted Master Key
            String privateKeyString = kdcIn.readLine(); // Bob's Private Key
            kdcSocket.close(); // Close connection to KDC

            // Decode and reconstruct Private Key
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Decrypt Bob’s Master Key (KB)
            String KB = RSAUtil.decrypt(encryptedKB, privateKey);
            System.out.println("Client B Received Master Key: " + KB);

            // Start server to receive session key from KDC
            ServerSocket serverSocket = new ServerSocket(5001);
            System.out.println("Client B waiting for session key...");

            Socket socket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Receive encrypted KAB from KDC
            String encryptedKABForBob = in.readLine();
            System.out.println("Client B received encrypted session key.");

            // Decrypt session key
            String KAB = RSAUtil.decrypt(encryptedKABForBob, privateKey);
            System.out.println("Client B Decrypted Session Key: " + KAB); 

            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



