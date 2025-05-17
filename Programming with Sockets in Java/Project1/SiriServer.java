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

    // Populate the Q/A map and knock-knock jokes
    static {
        qaMap.put("who created you?", "I was created by Apple.");
        qaMap.put("what does siri mean?", "Victory and beautiful.");
        qaMap.put("are you a robot?", "I am a virtual assistant.");
        qaMap.put("tell me a joke", "Knock, knock!");

        // Add knock-knock jokes
        knockKnockJokes.add(new String[]{"Interrupting cow", "Interrupting cow wh-- MOO!"});
        knockKnockJokes.add(new String[]{"Boo", "Boo who? Don't cry, it's just a joke!"});
        knockKnockJokes.add(new String[]{"Atch", "Atch who? Bless you!"});
        knockKnockJokes.add(new String[]{"Lettuce", "Lettuce in, it's cold out here!"});
        knockKnockJokes.add(new String[]{"Orange", "Orange you glad I didn't say banana?"});
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("SiriServer is running. Waiting for a client...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            // Input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String encryptedMessage;
            boolean waitingForKnockKnockResponse = false;
            String[] currentKnockKnockJoke = null;

            while ((encryptedMessage = in.readLine()) != null) {
                // Debugging: Print encrypted message received
                System.out.println("Received (Encrypted): " + encryptedMessage);

                // Decrypt the received message
                String decryptedMessage = decryptVigenere(encryptedMessage, VIGENERE_KEY);
                System.out.println("Decrypted Message: " + decryptedMessage); // Debugging: Print decrypted message

                String response;
                if (waitingForKnockKnockResponse) {
                    // Handle the next part of the knock-knock joke
                    if (currentKnockKnockJoke != null) {
                        response = currentKnockKnockJoke[1]; // Punchline
                        waitingForKnockKnockResponse = false; // End the joke
                        currentKnockKnockJoke = null;
                    } else {
                        response = "Sorry, I got confused!";
                    }
                } else if (decryptedMessage.equalsIgnoreCase("Knock, knock!")) {
                    // Start a knock-knock joke
                    currentKnockKnockJoke = knockKnockJokes.get(new Random().nextInt(knockKnockJokes.size()));
                    response = currentKnockKnockJoke[0]; // Setup
                    waitingForKnockKnockResponse = true;
                } else {
                    // Regular Q/A
                    response = getResponse(decryptedMessage);
                }

                // Encrypt the response
                String encryptedResponse = encryptVigenere(response, VIGENERE_KEY);
                System.out.println("Sending (Encrypted): " + encryptedResponse); // Debugging: Print encrypted response

                // Send the encrypted response
                out.println(encryptedResponse);
            }

            System.out.println("Client disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Encrypt message using Vigenère Cipher
    private static String encryptVigenere(String text, String key) {
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
    private static String decryptVigenere(String text, String key) {
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
    private static String getResponse(String question) {
        // Normalize input for case-insensitive matching
        String normalizedQuestion = question.toLowerCase();
        System.out.println("Normalized Question: " + normalizedQuestion); // Debugging: Print normalized question
        return qaMap.getOrDefault(normalizedQuestion, "Sorry, I don't understand that.");
    }
}
