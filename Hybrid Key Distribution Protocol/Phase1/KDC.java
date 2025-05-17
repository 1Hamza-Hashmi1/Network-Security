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

public class KDC {
    public static void main(String[] args) {
        try {
            // Generate RSA Key Pairs for KDC, A, and B
            KeyPair kdcKeys = RSAUtil.generateRSAKeyPair();
            KeyPair clientAKeys = RSAUtil.generateRSAKeyPair();  // Generate A's key
            KeyPair clientBKeys = RSAUtil.generateRSAKeyPair();  // Generate B's key

            PublicKey PUA = clientAKeys.getPublic();
            PrivateKey PRA = clientAKeys.getPrivate();  // Store A’s private key
            PublicKey PUB = clientBKeys.getPublic();
            PrivateKey PRB = clientBKeys.getPrivate();  // Store B’s private key
            PrivateKey PRK = kdcKeys.getPrivate();

            // Generate Master Keys (KA, KB)
            String KA = "MasterKeyA123456"; // Simulated shared secret
            String KB = "MasterKeyB123456";

            // Encrypt KA and KB using Client A and B’s public keys
            String encryptedKA = RSAUtil.encrypt(KA, PUA);
            String encryptedKB = RSAUtil.encrypt(KB, PUB);

            // Debug Prints: Print Encrypted Keys Before Sending
            System.out.println("===== KDC Debugging =====");
            System.out.println("Public Key A: " + Base64.getEncoder().encodeToString(PUA.getEncoded()));
            System.out.println("Private Key A: " + Base64.getEncoder().encodeToString(PRA.getEncoded()));
            System.out.println("Public Key B: " + Base64.getEncoder().encodeToString(PUB.getEncoded()));
            System.out.println("Private Key B: " + Base64.getEncoder().encodeToString(PRB.getEncoded()));
            System.out.println("Encrypted KA Sent to Client A: " + encryptedKA);
            System.out.println("Encrypted KB Sent to Client B: " + encryptedKB);
            System.out.println("=========================\n");

            // Start Server
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("KDC is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String clientRequest = in.readLine();
                if (clientRequest.equals("A")) {
                    out.println(encryptedKA);
                    out.println(Base64.getEncoder().encodeToString(PRA.getEncoded())); // Send Client A’s Private Key
                } else if (clientRequest.equals("B")) {
                    out.println(encryptedKB);
                    out.println(Base64.getEncoder().encodeToString(PRB.getEncoded())); // Send Client B’s Private Key
                }

                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


