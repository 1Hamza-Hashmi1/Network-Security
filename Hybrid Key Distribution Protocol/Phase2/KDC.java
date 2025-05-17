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
            KeyPair clientAKeys = RSAUtil.generateRSAKeyPair();
            KeyPair clientBKeys = RSAUtil.generateRSAKeyPair();

            PublicKey PUA = clientAKeys.getPublic();
            PrivateKey PRA = clientAKeys.getPrivate();
            PublicKey PUB = clientBKeys.getPublic();
            PrivateKey PRB = clientBKeys.getPrivate();

            // Generate Master Keys (KA, KB)
            String KA = "MasterKeyA123456";
            String KB = "MasterKeyB123456";

            // Encrypt KA and KB using Client A and B’s public keys
            String encryptedKA = RSAUtil.encrypt(KA, PUA);
            String encryptedKB = RSAUtil.encrypt(KB, PUB);

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
                    out.println(Base64.getEncoder().encodeToString(PRA.getEncoded()));
                    clientSocket.close(); // Close Alice connection
                } 
                else if (clientRequest.equals("B")) {
                    out.println(encryptedKB);
                    out.println(Base64.getEncoder().encodeToString(PRB.getEncoded()));
                    clientSocket.close(); // Close Bob connection
                } 
                else if (clientRequest.equals("SESSION_REQUEST")) {
                    System.out.println("KDC received session key request from Alice.");

                    // Step 1: Generate a Session Key KAB
                    String KAB = "SessionKey123456";

                    // Step 2: Encrypt session keys
                    String encryptedKABForAlice = RSAUtil.encrypt(KAB, PUA);
                    String encryptedKABForBob = RSAUtil.encrypt(KAB, PUB);

                    // Step 3: Send encrypted session key to Alice
                    out.println(encryptedKABForAlice);
                    System.out.println("KDC sent encrypted session key to Alice.");
                    clientSocket.close(); // Close Alice session request

                    // Step 4: Send encrypted session key to Bob
                    try (Socket bobSocket = new Socket("localhost", 5001);
                         PrintWriter bobOut = new PrintWriter(bobSocket.getOutputStream(), true)) {
                        bobOut.println(encryptedKABForBob);
                        System.out.println("KDC sent encrypted session key to Bob.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


