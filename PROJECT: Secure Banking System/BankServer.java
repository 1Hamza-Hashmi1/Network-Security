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
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class BankServer {
    private static final String SHARED_KEY_STRING = "MDEyMzQ1Njc4OUFCQ0RFRg==";
    private static final Map<String, String> userCredentials = new ConcurrentHashMap<>();
    private static final Map<String, Double> userBalances = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("BankServer running on port 5000...\n");

        // Pre-populated test account
        userCredentials.put("John", "Cena");
        userBalances.put("John", 1000.0);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("ATM connected from: " + clientSocket.getInetAddress());
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                SecretKey sharedKey = CryptoUtils.getKeyFromString(SHARED_KEY_STRING);

                // Authentication phase
                String encryptedCreds = reader.readLine();
                String decryptedCreds = CryptoUtils.decrypt(encryptedCreds, sharedKey);
                String[] parts = decryptedCreds.split(":");

                if (parts.length < 3) {
                    writer.println("AUTH_FAILED: Invalid format.");
                    socket.close(); return;
                }

                String action = parts[0];
                String username = parts[1];
                String password = parts[2];

                System.out.println("Login received: " + username);

                if (action.equals("REGISTER")) {
                    if (userCredentials.containsKey(username)) {
                        writer.println("AUTH_FAILED: User exists.");
                        socket.close(); return;
                    } else {
                        userCredentials.put(username, password);
                        userBalances.put(username, 0.0);
                        writer.println("AUTH_SUCCESS");
                    }
                } else if (action.equals("LOGIN")) {
                    if (!userCredentials.containsKey(username) || !userCredentials.get(username).equals(password)) {
                        writer.println("AUTH_FAILED: Invalid credentials.");
                        socket.close(); return;
                    } else {
                        writer.println("AUTH_SUCCESS");
                    }
                } else {
                    writer.println("AUTH_FAILED: Unknown command.");
                    socket.close(); return;
                }

                // Key exchange
                SecretKey masterSecret = CryptoUtils.generateAESKey();
                String encryptedMaster = CryptoUtils.encrypt(CryptoUtils.keyToString(masterSecret), sharedKey);
                writer.println(encryptedMaster);

                SecretKey[] derivedKeys = CryptoUtils.deriveKeys(masterSecret);
                SecretKey encryptionKey = derivedKeys[0];
                SecretKey macKey = derivedKeys[1];

                System.out.println("Encryption Key: " + Base64.getEncoder().encodeToString(encryptionKey.getEncoded()));
                System.out.println("MAC Key:        " + Base64.getEncoder().encodeToString(macKey.getEncoded()));

                // Transaction phase
                while (true) {
                    String encryptedTransaction = reader.readLine();
                    if (encryptedTransaction == null) break;
                    String hmac = reader.readLine();
                    String transaction = CryptoUtils.decrypt(encryptedTransaction, encryptionKey);

                    System.out.println("Transaction received: " + transaction);

                    boolean valid = CryptoUtils.verifyHMAC(transaction, hmac, macKey);
                    System.out.println("MAC verification: " + (valid ? "PASSED" : "FAILED"));

                    if (!valid) {
                        writer.println("MAC verification failed. Transaction rejected.");
                        continue;
                    }

                    String response = processTransaction(username, transaction);
                    writer.println(response);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String processTransaction(String username, String msg) {
            String lower = msg.toLowerCase();
            double balance = userBalances.getOrDefault(username, 0.0);

            if (lower.startsWith("withdraw")) {
                double amount = extractAmount(msg);
                if (balance >= amount) {
                    userBalances.put(username, balance - amount);
                    return "Withdrawal of $" + amount + " successful. New balance: $" + userBalances.get(username);
                } else {
                    return "Insufficient funds. Your balance is $" + balance;
                }
            } else if (lower.startsWith("deposit")) {
                double amount = extractAmount(msg);
                userBalances.put(username, balance + amount);
                return "Deposit of $" + amount + " successful. New balance: $" + userBalances.get(username);
            } else if (lower.contains("balance")) {
                return "Your current balance is $" + balance;
            } else {
                return "Invalid transaction command.";
            }
        }

        private double extractAmount(String msg) {
            try {
                return Double.parseDouble(msg.replaceAll("[^0-9.]", ""));
            } catch (Exception e) {
                return 0.0;
            }
        }
    }
}