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
import java.util.*;

public class SiriServer {
    private static final String VIGENERE_KEY = "TMU";

    // Dictionary for Q/A messages
    private static final Map<String, String> qaMap = new HashMap<>();

    // List for knock-knock jokes
    private static final List<String[]> knockKnockJokes = new ArrayList<>();

    static {
        // Populate Q/A dictionary
        qaMap.put("who created you?", "I was created by Apple.");
        qaMap.put("what does siri mean?", "Victory and beautiful.");
        qaMap.put("are you a robot?", "I am a virtual assistant.");
        qaMap.put("tell me a joke", "Knock, knock!");

        // Populate knock-knock jokes
        knockKnockJokes.add(new String[]{"Interrupting cow", "Interrupting cow wh-- MOO!"});
        knockKnockJokes.add(new String[]{"Boo", "Boo who? Don't cry, it's just a joke!"});
        knockKnockJokes.add(new String[]{"Atch", "Atch who? Bless you!"});
        knockKnockJokes.add(new String[]{"Lettuce", "Lettuce in, it's cold out here!"});
        knockKnockJokes.add(new String[]{"Orange", "Orange you glad I didn't say banana?"});
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("SiriServerMulti is running. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                // Handle each client in a new thread
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private boolean waitingForKnockKnockResponse = false; // Tracks joke flow
        private String[] currentKnockKnockJoke = null; // Holds current joke setup and punchline

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String encryptedMessage;
                while ((encryptedMessage = in.readLine()) != null) {
                    System.out.println("Received (Encrypted): " + encryptedMessage);

                    // Decrypt the received message
                    String decryptedMessage = decryptVigenere(encryptedMessage, VIGENERE_KEY);
                    System.out.println("Decrypted Message: " + decryptedMessage);

                    String response;
                    if (waitingForKnockKnockResponse) {
                        // Handle joke continuation
                        response = currentKnockKnockJoke[1]; // Punchline
                        waitingForKnockKnockResponse = false; // End the joke
                        currentKnockKnockJoke = null;
                    } else if (decryptedMessage.equalsIgnoreCase("Knock, knock!")) {
                        // Start a new knock-knock joke
                        currentKnockKnockJoke = knockKnockJokes.get(new Random().nextInt(knockKnockJokes.size()));
                        response = currentKnockKnockJoke[0]; // Joke setup
                        waitingForKnockKnockResponse = true;
                    } else {
                        // Handle regular Q/A
                        response = getResponse(decryptedMessage);
                    }

                    // Encrypt the response
                    String encryptedResponse = encryptVigenere(response, VIGENERE_KEY);
                    System.out.println("Sending (Encrypted): " + encryptedResponse);

                    // Send the encrypted response
                    out.println(encryptedResponse);
                }

                System.out.println("Client disconnected.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Encrypt message using Vigenère Cipher
        private String encryptVigenere(String text, String key) {
            StringBuilder result = new StringBuilder();
            text = text.toUpperCase();
            key = key.toUpperCase();
            int keyLength = key.length();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.isLetter(c)) {
                    result.append((char) (((c - 'A') + (key.charAt(i % keyLength) - 'A')) % 26 + 'A'));
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        }

        // Decrypt message using Vigenère Cipher
        private String decryptVigenere(String text, String key) {
            StringBuilder result = new StringBuilder();
            text = text.toUpperCase();
            key = key.toUpperCase();
            int keyLength = key.length();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.isLetter(c)) {
                    result.append((char) (((c - 'A') - (key.charAt(i % keyLength) - 'A') + 26) % 26 + 'A'));
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        }

        // Generate a response based on the question
        private String getResponse(String question) {
            return qaMap.getOrDefault(question.toLowerCase(), "Sorry, I don't understand that.");
        }
    }
}
