/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author hamza
 */
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class KDC {
    private static final int PORT = 12345;
    private static SecretKey Ks; // Shared session key
    private static Map<String, ObjectOutputStream> clientOutputs = new HashMap<>(); // Stores client output streams
    private static Map<String, PublicKey> clientPublicKeys = new HashMap<>(); // Stores client public keys

    public static void main(String[] args) {
        try {
            // Generate a shared session key (Ks)
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            Ks = keyGen.generateKey();

            // Start the server
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("KDC Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                // Create ObjectOutputStream first to avoid deadlock
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                // Send the shared session key (Ks) to the client
                out.writeObject(Ks);
                out.flush();

                // Register the client
                String clientID = (String) in.readObject();
                PublicKey clientPublicKey = (PublicKey) in.readObject(); // Read the client's public key
                clientOutputs.put(clientID, out);
                clientPublicKeys.put(clientID, clientPublicKey);
                System.out.println("Client " + clientID + " connected.");

                // Forward the new client's public key to all existing clients
                for (Map.Entry<String, ObjectOutputStream> entry : clientOutputs.entrySet()) {
                    if (!entry.getKey().equals(clientID)) {
                        ObjectOutputStream targetOut = entry.getValue();
                        targetOut.writeObject("NEW_CLIENT"); // Notify clients of a new client
                        targetOut.writeObject(clientID);
                        targetOut.writeObject(clientPublicKey);
                        targetOut.flush();
                    }
                }

                // Forward existing clients' public keys to the new client
                for (Map.Entry<String, PublicKey> entry : clientPublicKeys.entrySet()) {
                    if (!entry.getKey().equals(clientID)) {
                        out.writeObject("EXISTING_CLIENT"); // Notify the new client of existing clients
                        out.writeObject(entry.getKey());
                        out.writeObject(entry.getValue());
                        out.flush();
                    }
                }

                // Forward messages to other clients
                while (true) {
                    byte[] encryptedMessage = (byte[]) in.readObject();
                    byte[] signature = (byte[]) in.readObject();
                    String senderID = (String) in.readObject();

                    for (Map.Entry<String, ObjectOutputStream> entry : clientOutputs.entrySet()) {
                        if (!entry.getKey().equals(senderID)) {
                            ObjectOutputStream targetOut = entry.getValue();
                            targetOut.writeObject(encryptedMessage);
                            targetOut.writeObject(signature);
                            targetOut.writeObject(senderID);
                            targetOut.flush();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
