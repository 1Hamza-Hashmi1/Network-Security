/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author hamza
 */
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;
    private SecretKey Ks; // Shared session key
    private String clientID;
    private PrivateKey privateKey; // Client's private key for signing
    private PublicKey publicKey; // Client's public key for verification
    private final Map<String, PublicKey> publicKeys = new HashMap<>(); // Stores public keys of other clients
    private final Map<String, Integer> lastSequenceNumbers = new HashMap<>(); // Stores the last sequence number from each client
    private int sequenceNumber = 0; // Sequence number for outgoing messages

    public Client(String clientID) {
        this.clientID = clientID;
        generateKeyPair(); // Generate RSA key pair for this client
    }

    public void start() {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            // Create ObjectOutputStream first to avoid deadlock
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Receive the shared session key (Ks) from KDC
            Ks = (SecretKey) in.readObject();
            System.out.println("Received session key from KDC.");

            // Send client ID and public key to KDC
            out.writeObject(clientID);
            out.writeObject(publicKey); // Send the PublicKey object directly
            out.flush();

            // Start a thread to listen for incoming messages and public keys
            new Thread(new MessageListener(in)).start();

            // Send messages to other clients
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter message (or 'replay' to simulate replay attack): ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("replay")) {
                    // Simulate a replay attack
                    simulateReplayAttack(out);
                } else {
                    // Send a normal message
                    sendMessage(out, input);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(ObjectOutputStream out, String message) throws Exception {
        // Increment the sequence number
        sequenceNumber++;

        // Add the sequence number to the message
        String messageWithSequenceNumber = message + "||" + sequenceNumber;

        // Encrypt the message
        byte[] encryptedMessage = encryptMessage(messageWithSequenceNumber);

        // Sign the message
        byte[] signature = signMessage(messageWithSequenceNumber);

        // Send the encrypted message, signature, and sender ID to KDC
        out.writeObject(encryptedMessage);
        out.writeObject(signature);
        out.writeObject(clientID);
        out.flush();
    }

    private void simulateReplayAttack(ObjectOutputStream out) throws Exception {
        // Simulate capturing a valid message
        String capturedMessage = "Hello from " + clientID;
        int capturedSequenceNumber = sequenceNumber; // Use the current sequence number
        String capturedMessageWithSequenceNumber = capturedMessage + "||" + capturedSequenceNumber;

        // Encrypt the captured message
        byte[] encryptedMessage = encryptMessage(capturedMessageWithSequenceNumber);

        // Sign the captured message
        byte[] signature = signMessage(capturedMessageWithSequenceNumber);

        // Resend the captured message
        System.out.println("Simulating replay attack...");
        out.writeObject(encryptedMessage);
        out.writeObject(signature);
        out.writeObject(clientID);
        out.flush();
    }

    private byte[] encryptMessage(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, Ks);
        return cipher.doFinal(message.getBytes());
    }

    private byte[] signMessage(String message) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes());
        return signature.sign();
    }

    private boolean verifySignature(String message, byte[] signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(message.getBytes());
        return sig.verify(signature);
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MessageListener implements Runnable {
        private ObjectInputStream in;

        public MessageListener(ObjectInputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // Read the incoming object
                    Object obj = in.readObject();

                    if (obj instanceof String) {
                        // Handle public key updates
                        String command = (String) obj;
                        if (command.equals("NEW_CLIENT")) {
                            // Receive a new client's public key
                            String newClientID = (String) in.readObject();
                            PublicKey newClientPublicKey = (PublicKey) in.readObject();
                            publicKeys.put(newClientID, newClientPublicKey);
                            System.out.println("\nReceived public key for " + newClientID);
                        } else if (command.equals("EXISTING_CLIENT")) {
                            // Receive an existing client's public key
                            String existingClientID = (String) in.readObject();
                            PublicKey existingClientPublicKey = (PublicKey) in.readObject();
                            publicKeys.put(existingClientID, existingClientPublicKey);
                            System.out.println("\nReceived public key for " + existingClientID);
                        }
                    } else if (obj instanceof byte[]) {
                        // Handle encrypted messages
                        byte[] encryptedMessage = (byte[]) obj;
                        byte[] signature = (byte[]) in.readObject();
                        String senderID = (String) in.readObject();

                        // Decrypt the message
                        String decryptedMessageWithSequenceNumber = decryptMessage(encryptedMessage);

                        // Split the message and sequence number
                        String[] parts = decryptedMessageWithSequenceNumber.split("\\|\\|");
                        if (parts.length < 2) {
                            System.out.println("\nInvalid message format from " + senderID);
                            continue;
                        }
                        String decryptedMessage = parts[0];
                        int sequenceNumber = Integer.parseInt(parts[1]);

                        // Check for replay attacks
                        if (isReplayAttack(senderID, sequenceNumber)) {
                            System.out.println("\nReplay attack detected from " + senderID + ". Message ignored.");
                            continue;
                        }

                        // Verify the signature
                        PublicKey senderPublicKey = publicKeys.get(senderID);
                        if (senderPublicKey == null) {
                            System.out.println("\nPublic key for " + senderID + " not found.");
                            continue;
                        }

                        boolean isVerified = verifySignature(decryptedMessageWithSequenceNumber, signature, senderPublicKey);
                        if (isVerified) {
                            System.out.println("\nReceived from " + senderID + ": " + decryptedMessage);
                        } else {
                            System.out.println("\nReceived a message from " + senderID + " with an invalid signature.");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String decryptMessage(byte[] encryptedMessage) throws Exception {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, Ks);
            byte[] decryptedBytes = cipher.doFinal(encryptedMessage);
            return new String(decryptedBytes);
        }

        private boolean isReplayAttack(String senderID, int sequenceNumber) {
            int lastSequenceNumber = lastSequenceNumbers.getOrDefault(senderID, -1);
            if (sequenceNumber <= lastSequenceNumber) {
                return true; // Replay attack detected
            }
            lastSequenceNumbers.put(senderID, sequenceNumber);
            return false;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter client ID (A, B, or C): ");
        String clientID = scanner.nextLine();

        Client client = new Client(clientID);
        client.start();
    }
}