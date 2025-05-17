/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author hamza
 */
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.crypto.SecretKey;

public class BankServerGUI {
    private JFrame frame;
    private JTextArea logArea;
    private JLabel statusLabel;
    private final int PORT = 5000;
    private HashMap<String, String> userDatabase = new HashMap<>();
    private HashMap<String, Double> accountBalances = new HashMap<>();
    private final String SHARED_KEY_STRING = "MDEyMzQ1Njc4OUFCQ0RFRg==";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new BankServerGUI().startServer();
        });
    }

    private void startServer() {
        setupGUI();

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                log("Bank Server started on port " + PORT);
                log("Waiting for ATM connections...");

                while (true) {
                    Socket socket = serverSocket.accept();
                    log("\nATM connected: " + socket.getInetAddress());
                    new ClientHandler(socket).start();
                }
            } catch (IOException e) {
                log("Server error: " + e.getMessage());
            }
        }).start();
    }

    private void setupGUI() {
        frame = new JFrame("Secure Banking Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(0, 102, 204);
                Color color2 = new Color(0, 51, 102);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("SECURE BANKING SERVER", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        headerPanel.add(title);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setOpaque(false);
        logPanel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 1, 1, 1, new Color(200, 200, 200)),
            new EmptyBorder(10, 10, 10, 10)
        ));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        logArea.setForeground(Color.WHITE);
        logArea.setOpaque(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel(" Status: Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 200, 200));
        statusLabel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(100, 100, 100)));

        mainPanel.add(logPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("> " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            statusLabel.setText(" Status: " + message);
        });
    }

    private class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                SecretKey sharedKey = CryptoUtils.getKeyFromString(SHARED_KEY_STRING);

                // Authentication phase
                String encryptedCreds = reader.readLine();
                String creds = CryptoUtils.decrypt(encryptedCreds, sharedKey);
                String[] parts = creds.split(":");
                String action = parts[0];
                String username = parts[1];
                String password = parts[2];

                log("Authentication request from: " + username);
                log("Action: " + action);

                if ("REGISTER".equals(action)) {
                    userDatabase.put(username, password);
                    accountBalances.put(username, 1000.0);
                    writer.println("AUTH_SUCCESS");
                    log("New user registered: " + username);
                } else if (!password.equals(userDatabase.get(username))) {
                    writer.println("AUTH_FAILED");
                    log("Authentication failed for: " + username);
                    return;
                } else {
                    writer.println("AUTH_SUCCESS");
                    log("Authentication successful for: " + username);
                }

                // Key exchange
                SecretKey masterSecret = CryptoUtils.generateAESKey();
                String encryptedMaster = CryptoUtils.encrypt(Base64.getEncoder().encodeToString(masterSecret.getEncoded()), sharedKey);
                writer.println(encryptedMaster);
                log("Master key exchanged with: " + username);

                SecretKey[] derivedKeys = CryptoUtils.deriveKeys(masterSecret);
                SecretKey encryptionKey = derivedKeys[0];
                SecretKey macKey = derivedKeys[1];

                // Transaction processing
                while (true) {
                    String encryptedMessage = reader.readLine();
                    String receivedMAC = reader.readLine();
                    if (encryptedMessage == null || receivedMAC == null) break;

                    String decrypted = CryptoUtils.decrypt(encryptedMessage, encryptionKey);
                    boolean valid = CryptoUtils.verifyHMAC(decrypted, receivedMAC, macKey);

                    log("\nTransaction from: " + username);
                    log("Type: " + decrypted);
                    log("MAC verification: " + (valid ? "VALID" : "INVALID"));

                    if (!valid) {
                        writer.println("MAC verification failed!");
                        continue;
                    }

                    String response = processTransaction(username, decrypted);
                    writer.println(response);
                    log("Response: " + response);
                }

            } catch (Exception e) {
                log("Client error: " + e.getMessage());
            } finally {
                log("Connection closed: " + socket.getInetAddress());
            }
        }

        private String processTransaction(String username, String transaction) {
            transaction = transaction.toLowerCase();
            double balance = accountBalances.get(username);

            if (transaction.startsWith("withdraw")) {
                double amount = Double.parseDouble(transaction.replaceAll("[^0-9.]", ""));
                if (balance >= amount) {
                    accountBalances.put(username, balance - amount);
                    return "Withdrawn $" + amount + ". New balance: $" + accountBalances.get(username);
                } else {
                    return "Insufficient funds.";
                }
            } else if (transaction.startsWith("deposit")) {
                double amount = Double.parseDouble(transaction.replaceAll("[^0-9.]", ""));
                accountBalances.put(username, balance + amount);
                return "Deposited $" + amount + ". New balance: $" + accountBalances.get(username);
            } else if (transaction.equals("balance")) {
                return "Balance: $" + balance;
            }
            return "Invalid transaction.";
        }
    }
}

