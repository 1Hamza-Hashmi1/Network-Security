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
import java.util.Scanner;

public class ATMClient {
    private static final String SHARED_KEY_STRING = "MDEyMzQ1Njc4OUFCQ0RFRg==";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Socket socket = new Socket("localhost", 5000);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("===== Welcome to ATM =====");
        System.out.print("Do you want to [login] or [register]? ");
        String choice = scanner.nextLine().trim().toLowerCase();

        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        String action = choice.equals("register") ? "REGISTER" : "LOGIN";
        String credentials = action + ":" + username + ":" + password;

        SecretKey sharedKey = CryptoUtils.getKeyFromString(SHARED_KEY_STRING);
        String encryptedCreds = CryptoUtils.encrypt(credentials, sharedKey);
        writer.println(encryptedCreds);

        String serverResponse = reader.readLine();
        if (!serverResponse.equals("AUTH_SUCCESS")) {
            System.out.println("Authentication failed: " + serverResponse);
            socket.close();
            return;
        }

        // Receive and decrypt master secret
        String encryptedMaster = reader.readLine();
        String decodedKey = CryptoUtils.decrypt(encryptedMaster, sharedKey);
        SecretKey masterSecret = CryptoUtils.getKeyFromString(decodedKey);
        SecretKey[] derivedKeys = CryptoUtils.deriveKeys(masterSecret);
        SecretKey encryptionKey = derivedKeys[0];
        SecretKey macKey = derivedKeys[1];

        System.out.println("\nLogin successful. Transaction session started.");

        while (true) {
            System.out.print("Enter transaction ([Deposit $100], [Withdraw $50], [Balance], or 'exit'): ");
            String transaction = scanner.nextLine();
            if (transaction.equalsIgnoreCase("exit")) break;

            System.out.print("Tamper with MAC? (y/n): ");
            String tamperChoice = scanner.nextLine().trim().toLowerCase();

            String encryptedTransaction = CryptoUtils.encrypt(transaction, encryptionKey);
            String hmac = CryptoUtils.generateHMAC(transaction, macKey);

            if (tamperChoice.equals("y")) {
                hmac = "INVALIDMAC123";
                System.out.println("[!] Sending transaction with tampered MAC!");
            }

            writer.println(encryptedTransaction);
            writer.println(hmac);

            String response = reader.readLine();
            System.out.println("Bank Response: " + response);
        }

        socket.close();
        System.out.println("Session closed.");
    }
}
