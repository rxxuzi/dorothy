package ui;

import cipher.RSAManager;
import model.TextChunk;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class TextEditorPanel extends JPanel {
    private JTextArea textArea;
    private JTextField keywordField;
    private JButton encryptButton;
    private JButton decryptButton;
    private JButton updateButton;
    private JButton clearButton;
    private JLabel statusLabel;
    private JLabel bytesLabel;

    private final DoroFrame parent;
    private TextChunk currentChunk;
    private int currentIndex;

    public TextEditorPanel(DoroFrame parent) {
        this.parent = parent;
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                        "Text Content",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.PLAIN, 12),
                        new Color(100, 100, 100)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Top panel - keyword and status
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBackground(Color.WHITE);

        JPanel keywordPanel = new JPanel(new BorderLayout(5, 0));
        keywordPanel.setBackground(Color.WHITE);

        JLabel keywordLabel = new JLabel("Keyword:");
        keywordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        keywordLabel.setForeground(new Color(60, 60, 60));

        keywordField = new JTextField("Comment");
        keywordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        keywordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        keywordPanel.add(keywordLabel, BorderLayout.WEST);
        keywordPanel.add(keywordField, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusPanel.setBackground(Color.WHITE);

        statusLabel = new JLabel("No chunk selected");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(new Color(120, 120, 120));

        bytesLabel = new JLabel("");
        bytesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bytesLabel.setForeground(new Color(120, 120, 120));

        statusPanel.add(statusLabel);
        statusPanel.add(bytesLabel);

        topPanel.add(keywordPanel, BorderLayout.NORTH);
        topPanel.add(statusPanel, BorderLayout.SOUTH);

        // Center panel - text area
        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textArea.setBackground(new Color(250, 250, 250));

        // Add document listener for real-time byte count
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateByteCount(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateByteCount(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateByteCount(); }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Bottom panel - buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        // Create modern styled buttons
        encryptButton = createModernButton("Encrypt", new Color(255, 107, 129));
        decryptButton = createModernButton("Decrypt", new Color(156, 89, 182));
        updateButton = createModernButton("Update", new Color(66, 165, 245));
        clearButton = createModernButton("Clear", new Color(120, 120, 120));

        // Add action listeners
        encryptButton.addActionListener(e -> encryptCurrentChunk());
        decryptButton.addActionListener(e -> decryptCurrentChunk());
        updateButton.addActionListener(e -> updateCurrentChunk());
        clearButton.addActionListener(e -> clearText());

        // Add keyboard shortcut for update (Ctrl+S)
        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), "update");
        textArea.getActionMap().put("update", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCurrentChunk();
            }
        });

        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(updateButton);
        buttonPanel.add(clearButton);

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(new Color(245, 245, 245));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel infoLabel = new JLabel("Text is securely encrypted using AES+RSA");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(150, 150, 150));
        infoPanel.add(infoLabel);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Color.WHITE);
        bottomContainer.add(buttonPanel, BorderLayout.CENTER);
        bottomContainer.add(infoPanel, BorderLayout.SOUTH);

        // Add components
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);

        // Initially disable all controls
        setControlsEnabled(false);
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button background
                if (getModel().isPressed()) {
                    g2d.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(color.brighter());
                } else if (!isEnabled()) {
                    g2d.setColor(new Color(230, 230, 230));
                } else {
                    g2d.setColor(color);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Button text
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                if (!isEnabled()) {
                    g2d.setColor(new Color(180, 180, 180));
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.drawString(getText(), x, y);
            }
        };

        button.setPreferredSize(new Dimension(100, 35));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void updateByteCount() {
        if (textArea.isEditable()) {
            int bytes = textArea.getText().getBytes().length;
            bytesLabel.setText("(" + bytes + " bytes)");
            bytesLabel.setForeground(new Color(120, 120, 120));
        }
    }

    public void loadChunk(TextChunk chunk, int index) {
        this.currentChunk = chunk;
        this.currentIndex = index;

        keywordField.setText(chunk.getKeyword());
        textArea.setText(chunk.getText());

        boolean isEncrypted = chunk.isEncrypted();
        textArea.setEditable(!isEncrypted);
        keywordField.setEditable(!isEncrypted);
        encryptButton.setEnabled(!isEncrypted);
        decryptButton.setEnabled(isEncrypted);
        updateButton.setEnabled(!isEncrypted);
        clearButton.setEnabled(!isEncrypted);

        setControlsEnabled(true);

        if (isEncrypted) {
            statusLabel.setText("Chunk " + (index + 1) + " - Encrypted");
            statusLabel.setForeground(new Color(220, 50, 50));
            textArea.setBackground(new Color(255, 250, 250));
            bytesLabel.setText("(Encrypted)");
        } else {
            statusLabel.setText("Chunk " + (index + 1) + " - Plain Text");
            statusLabel.setForeground(new Color(60, 150, 60));
            textArea.setBackground(new Color(250, 250, 250));
            updateByteCount();
        }
    }

    private void encryptCurrentChunk() {
        if (currentChunk == null || currentChunk.isEncrypted()) return;

        String text = textArea.getText();

        try {
            RSAManager rsaManager = parent.getRSAManager();
            String encrypted = rsaManager.encrypt(text);

            currentChunk.setText(encrypted);
            currentChunk.setEncrypted(true);
            currentChunk.setKeyword(keywordField.getText());

            textArea.setText(encrypted);
            textArea.setEditable(false);
            textArea.setBackground(new Color(255, 250, 250));
            keywordField.setEditable(false);
            encryptButton.setEnabled(false);
            decryptButton.setEnabled(true);
            updateButton.setEnabled(false);
            clearButton.setEnabled(false);

            statusLabel.setText("Chunk " + (currentIndex + 1) + " - Encrypted");
            statusLabel.setForeground(new Color(220, 50, 50));
            bytesLabel.setText("(Encrypted)");

            parent.getChunkPanel().refreshDisplay();
            parent.log("Encrypted chunk " + (currentIndex + 1));

        } catch (Exception e) {
            parent.log("Encryption error: " + e.getMessage());
            JOptionPane.showMessageDialog(parent,
                    "Encryption failed: " + e.getMessage(),
                    "Encryption Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decryptCurrentChunk() {
        if (currentChunk == null || !currentChunk.isEncrypted()) return;

        try {
            RSAManager rsaManager = parent.getRSAManager();
            String decrypted = rsaManager.decrypt(currentChunk.getText());

            currentChunk.setText(decrypted);
            currentChunk.setEncrypted(false);

            textArea.setText(decrypted);
            textArea.setEditable(true);
            textArea.setBackground(new Color(250, 250, 250));
            keywordField.setEditable(true);
            encryptButton.setEnabled(true);
            decryptButton.setEnabled(false);
            updateButton.setEnabled(true);
            clearButton.setEnabled(true);

            statusLabel.setText("Chunk " + (currentIndex + 1) + " - Plain Text");
            statusLabel.setForeground(new Color(60, 150, 60));
            updateByteCount();

            parent.getChunkPanel().refreshDisplay();
            parent.log("Decrypted chunk " + (currentIndex + 1));

        } catch (Exception e) {
            parent.log("Decryption error: " + e.getMessage());
            JOptionPane.showMessageDialog(parent,
                    "Decryption failed. Make sure you have the correct private key.",
                    "Decryption Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCurrentChunk() {
        if (currentChunk == null || currentChunk.isEncrypted()) return;

        String newText = textArea.getText();
        String newKeyword = keywordField.getText().trim();

        if (newKeyword.isEmpty()) {
            newKeyword = "Comment";
            keywordField.setText(newKeyword);
        }

        currentChunk.setText(newText);
        currentChunk.setKeyword(newKeyword);

        // Auto-save to file
        try {
            parent.saveFile();
            parent.getChunkPanel().refreshDisplay();
            parent.log("Updated and saved chunk " + (currentIndex + 1));

            // Visual feedback
            statusLabel.setText("Chunk " + (currentIndex + 1) + " - Saved");
            Timer timer = new Timer(2000, e -> {
                statusLabel.setText("Chunk " + (currentIndex + 1) + " - Plain Text");
            });
            timer.setRepeats(false);
            timer.start();

        } catch (Exception e) {
            parent.log("Failed to save: " + e.getMessage());
        }
    }

    private void clearText() {
        if (currentChunk != null && !currentChunk.isEncrypted()) {
            textArea.setText("");
            updateCurrentChunk();
        }
    }

    public void clearEditor() {
        currentChunk = null;
        currentIndex = -1;
        textArea.setText("");
        keywordField.setText("Comment");
        statusLabel.setText("No chunk selected");
        statusLabel.setForeground(new Color(120, 120, 120));
        bytesLabel.setText("");
        setControlsEnabled(false);
    }

    private void setControlsEnabled(boolean enabled) {
        textArea.setEnabled(enabled);
        keywordField.setEnabled(enabled);
        encryptButton.setEnabled(enabled);
        decryptButton.setEnabled(enabled);
        updateButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
    }
}