/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author hamza
 */
import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class ATMClientGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextArea logArea;
    private JButton loginButton, registerButton, transactionButton;
    private JComboBox<String> transactionType;
    private JTextField amountField;
    private JCheckBox tamperCheckBox;
    private PrintWriter writer;
    private BufferedReader reader;
    private SecretKey encryptionKey, macKey;

    public ATMClientGUI() {
        setTitle("Secure ATM Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        // Main panel with card layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        JLabel titleLabel = new JLabel("SECURE ATM CLIENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Authentication tab
        JPanel authPanel = new JPanel(new GridBagLayout());
        authPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        authPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel authTitle = new JLabel("Authentication");
        authTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        authPanel.add(authTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        authPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        authPanel.add(usernameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        authPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        authPanel.add(passwordField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        loginButton = new JButton("Login");
        styleButton(loginButton, new Color(76, 175, 80));
        authPanel.add(loginButton, gbc);

        gbc.gridx = 1;
        registerButton = new JButton("Register");
        styleButton(registerButton, new Color(33, 150, 243));
        authPanel.add(registerButton, gbc);

        // Transaction tab
        JPanel transactionPanel = new JPanel(new GridBagLayout());
        transactionPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        transactionPanel.setBackground(Color.WHITE);

        JLabel transTitle = new JLabel("Bank Transactions");
        transTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        transactionPanel.add(transTitle, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        transactionPanel.add(new JLabel("Transaction Type:"), gbc);
        gbc.gridx = 1;
        transactionType = new JComboBox<>(new String[]{"Balance Inquiry", "Withdraw", "Deposit"});
        transactionType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionPanel.add(transactionType, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        transactionPanel.add(new JLabel("Amount ($):"), gbc);
        gbc.gridx = 1;

        // Create the improved amount field
        amountField = new JTextField();
        amountField.setFont(new Font("Segoe UI", Font.BOLD, 18));  // Larger, bold font
        amountField.setPreferredSize(new Dimension(200, 40));  // Wider and taller
        amountField.setMinimumSize(new Dimension(200, 40));  // Prevent shrinking
        amountField.setHorizontalAlignment(JTextField.RIGHT);  // Right-align numbers

        // Add visual enhancements
        amountField.setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(new Color(150, 150, 150)),  // Outer border with closing parenthesis
    BorderFactory.createEmptyBorder(5, 10, 5, 10)  // Inner padding
        ));

        // Make the field stand out more
        amountField.setBackground(Color.WHITE);
        amountField.setForeground(new Color(0, 100, 0));  // Dark green for currency

        // Add to panel with proper constraints
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;  // Allow horizontal expansion
        transactionPanel.add(amountField, gbc);

        // Reset constraints for next components
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;

        gbc.gridy = 3; gbc.gridx = 0;
        tamperCheckBox = new JCheckBox("Simulate MAC Tampering");
        tamperCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionPanel.add(tamperCheckBox, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        transactionButton = new JButton("Execute Transaction");
        styleButton(transactionButton, new Color(156, 39, 176));
        transactionPanel.add(transactionButton, gbc);

        // Log area
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBorder(new CompoundBorder(
            new MatteBorder(1, 1, 1, 1, new Color(200, 200, 200)),
            new EmptyBorder(10, 10, 10, 10)
        ));

        tabbedPane.addTab("Authentication", authPanel);
        tabbedPane.addTab("Transactions", transactionPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(new JScrollPane(logArea), BorderLayout.SOUTH);

        // Event handlers
        loginButton.addActionListener(e -> handleAuth("LOGIN"));
        registerButton.addActionListener(e -> handleAuth("REGISTER"));
        transactionButton.addActionListener(this::handleTransaction);

        add(mainPanel);
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void handleAuth(String action) {
        try {
            Socket socket = new Socket("localhost", 5000);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            SecretKey sharedKey = CryptoUtils.getKeyFromString("MDEyMzQ1Njc4OUFCQ0RFRg==");
            String credentials = action + ":" + usernameField.getText() + ":" + new String(passwordField.getPassword());
            String encryptedCreds = CryptoUtils.encrypt(credentials, sharedKey);
            writer.println(encryptedCreds);

            String response = reader.readLine();
            if (!"AUTH_SUCCESS".equals(response)) {
                log("Authentication failed: " + response);
                JOptionPane.showMessageDialog(this, "Authentication failed!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String encryptedMaster = reader.readLine();
            String masterKeyStr = CryptoUtils.decrypt(encryptedMaster, sharedKey);
            SecretKey masterSecret = CryptoUtils.getKeyFromString(masterKeyStr);
            SecretKey[] keys = CryptoUtils.deriveKeys(masterSecret);
            encryptionKey = keys[0];
            macKey = keys[1];

            log("Authentication successful!");
            log("Session keys established with bank server");
        } catch (Exception ex) {
            log("Error during authentication: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleTransaction(ActionEvent e) {
        try {
            if (writer == null || encryptionKey == null || macKey == null) {
                JOptionPane.showMessageDialog(this, "Please authenticate first!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String type = (String) transactionType.getSelectedItem();
            String amount = amountField.getText().trim();
            String message = type.equals("Balance Inquiry") ? "BALANCE" : 
                           type.toUpperCase() + " " + (amount.isEmpty() ? "0" : amount);

            String encrypted = CryptoUtils.encrypt(message, encryptionKey);
            String hmac = CryptoUtils.computeHMAC(tamperCheckBox.isSelected() ? "TAMPERED" : message, macKey);
            
            writer.println(encrypted);
            writer.println(hmac);

            String response = reader.readLine();
            log("\nTransaction: " + message);
            log("Server Response: " + response);
        } catch (Exception ex) {
            log("Transaction error: " + ex.getMessage());
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("> " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ATMClientGUI client = new ATMClientGUI();
            client.setVisible(true);
        });
    }
}