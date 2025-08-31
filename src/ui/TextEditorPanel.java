package ui;

import cipher.RSAManager;
import doro.DoroStyle;
import model.TextChunk;

import javax.swing.*;
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
                DoroStyle.createSubtitledBorder("Text Content"),
                BorderFactory.createEmptyBorder(DoroStyle.PADDING_MEDIUM, DoroStyle.PADDING_MEDIUM, 
                                               DoroStyle.PADDING_MEDIUM, DoroStyle.PADDING_MEDIUM)
        ));

        // Top panel - keyword and status
        JPanel topPanel = DoroStyle.createPanel();
        topPanel.setLayout(new BorderLayout(DoroStyle.PADDING_MEDIUM, DoroStyle.PADDING_SMALL));

        JPanel keywordPanel = DoroStyle.createPanel();
        keywordPanel.setLayout(new BorderLayout(DoroStyle.PADDING_SMALL, 0));

        JLabel keywordLabel = new JLabel("Keyword:");
        keywordLabel.setFont(DoroStyle.FONT_DEFAULT);
        keywordLabel.setForeground(DoroStyle.TEXT_PRIMARY);

        keywordField = DoroStyle.createTextField("Comment");

        keywordPanel.add(keywordLabel, BorderLayout.WEST);
        keywordPanel.add(keywordField, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = DoroStyle.createPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, DoroStyle.PADDING_MEDIUM, 0));

        statusLabel = new JLabel("No chunk selected");
        statusLabel.setFont(DoroStyle.FONT_ITALIC);
        statusLabel.setForeground(DoroStyle.TEXT_SECONDARY);

        bytesLabel = new JLabel("");
        bytesLabel.setFont(DoroStyle.FONT_SMALL);
        bytesLabel.setForeground(DoroStyle.TEXT_SECONDARY);

        statusPanel.add(statusLabel);
        statusPanel.add(bytesLabel);

        topPanel.add(keywordPanel, BorderLayout.NORTH);
        topPanel.add(statusPanel, BorderLayout.SOUTH);

        // Center panel - text area
        textArea = DoroStyle.createTextArea();

        // Add document listener for real-time byte count
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateByteCount(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateByteCount(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateByteCount(); }
        });

        JScrollPane scrollPane = DoroStyle.createScrollPane(textArea);

        // Bottom panel - buttons
        JPanel buttonPanel = DoroStyle.createPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, DoroStyle.PADDING_MEDIUM, DoroStyle.PADDING_MEDIUM));

        // Create styled buttons
        encryptButton = DoroStyle.createButton("Encrypt", DoroStyle.DORO_DARK_PINK);
        decryptButton = DoroStyle.createButton("Decrypt", DoroStyle.DORO_PURPLE);
        updateButton = DoroStyle.createButton("Update", DoroStyle.SUCCESS_GREEN);
        clearButton = DoroStyle.createButton("Clear", DoroStyle.TEXT_SECONDARY);

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
        infoPanel.setBackground(DoroStyle.BACKGROUND_LIGHTER);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(DoroStyle.PADDING_SMALL, DoroStyle.PADDING_MEDIUM, 
                                                           DoroStyle.PADDING_SMALL, DoroStyle.PADDING_MEDIUM));

        JLabel infoLabel = new JLabel("Text is securely encrypted using AES+RSA");
        infoLabel.setFont(DoroStyle.FONT_ITALIC);
        infoLabel.setForeground(DoroStyle.TEXT_DISABLED);
        infoPanel.add(infoLabel);

        JPanel bottomContainer = DoroStyle.createPanel();
        bottomContainer.setLayout(new BorderLayout());
        bottomContainer.add(buttonPanel, BorderLayout.CENTER);
        bottomContainer.add(infoPanel, BorderLayout.SOUTH);

        // Add components
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);

        // Initially disable all controls
        setControlsEnabled(false);
    }


    private void updateByteCount() {
        if (textArea.isEditable()) {
            int bytes = textArea.getText().getBytes().length;
            bytesLabel.setText("(" + bytes + " bytes)");
            bytesLabel.setForeground(DoroStyle.TEXT_SECONDARY);
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
            statusLabel.setForeground(DoroStyle.ERROR_RED);
            textArea.setBackground(DoroStyle.DORO_WHITE);
            bytesLabel.setText("(Encrypted)");
        } else {
            statusLabel.setText("Chunk " + (index + 1) + " - Plain Text");
            statusLabel.setForeground(DoroStyle.SUCCESS_GREEN);
            textArea.setBackground(DoroStyle.BACKGROUND_LIGHT);
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
            textArea.setBackground(DoroStyle.DORO_WHITE);
            keywordField.setEditable(false);
            encryptButton.setEnabled(false);
            decryptButton.setEnabled(true);
            updateButton.setEnabled(false);
            clearButton.setEnabled(false);

            statusLabel.setText("Chunk " + (currentIndex + 1) + " - Encrypted");
            statusLabel.setForeground(DoroStyle.ERROR_RED);
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
            textArea.setBackground(DoroStyle.BACKGROUND_LIGHT);
            keywordField.setEditable(true);
            encryptButton.setEnabled(true);
            decryptButton.setEnabled(false);
            updateButton.setEnabled(true);
            clearButton.setEnabled(true);

            statusLabel.setText("Chunk " + (currentIndex + 1) + " - Plain Text");
            statusLabel.setForeground(DoroStyle.SUCCESS_GREEN);
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
            Timer timer = new Timer(2000, e -> statusLabel.setText("Chunk " + (currentIndex + 1) + " - Plain Text"));
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
        statusLabel.setForeground(DoroStyle.TEXT_SECONDARY);
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