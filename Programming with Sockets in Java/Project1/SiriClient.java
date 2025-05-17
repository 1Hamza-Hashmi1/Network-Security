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
import java.util.Scanner;

public class SiriClient {
    private static final String VIGENERE_KEY = "TMU";

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            System.out.println("Connected to SiriServer.");

            // Input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            String question;
            while (true) {
                System.out.print("Enter your question (type 'exit' to quit): ");
                question = scanner.nextLine();
                if (question.equalsIgnoreCase("exit")) {
                    System.out.println("Closing connection.");
                    break;
                }

                // Encrypt the question
                String encryptedQuestion = encryptVigenere(question, VIGENERE_KEY);
                System.out.println("Sending (Encrypted): " + encryptedQuestion);

                // Send the encrypted question
                out.println(encryptedQuestion);

                // Receive the encrypted response
                String encryptedResponse = in.readLine();
                System.out.println("Received (Encrypted): " + encryptedResponse);

                // Decrypt the response
                String decryptedResponse = decryptVigenere(encryptedResponse, VIGENERE_KEY);
                System.out.println("Siri's Response (Decrypted): " + decryptedResponse);
            }

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
}
