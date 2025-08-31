package ui;

import model.TextChunk;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static doro.DoroStyle.renderButton;
import static ui.DoroFrame.*;

public class ChunkPanel extends JPanel {
    private JList<String> chunkList;
    private DefaultListModel<String> chunkListModel;
    private final DoroFrame parent;

    public ChunkPanel(DoroFrame parent) {
        this.parent = parent;
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(DOROTHY_WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(DOROTHY_LIGHT_PINK, 2),
                        "Text Chunks",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        new Color(100, 100, 100)  // Dark gray for title
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Create list with custom renderer
        chunkListModel = new DefaultListModel<>();
        chunkList = new JList<>(chunkListModel);
        chunkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chunkList.setBackground(DOROTHY_WHITE);
        chunkList.setSelectionBackground(DOROTHY_PURPLE);
        chunkList.setSelectionForeground(Color.WHITE);
        chunkList.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        chunkList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

                if (isSelected) {
                    label.setBackground(DOROTHY_PURPLE);
                    label.setForeground(Color.WHITE);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    label.setBackground(DOROTHY_WHITE);
                    if (value.toString().contains("[ENCRYPTED]")) {
                        label.setForeground(new Color(220, 50, 50));  // Red for encrypted
                        label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    } else {
                        label.setForeground(new Color(60, 60, 60));  // Dark gray for normal
                        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    }
                }

                return label;
            }
        });

        chunkList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedChunk();
            }
        });

        // Add context menu
        JPopupMenu popupMenu = createPopupMenu();
        chunkList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = chunkList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        chunkList.setSelectedIndex(index);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(chunkList);
        scrollPane.setBorder(BorderFactory.createLineBorder(DOROTHY_LIGHT_PINK, 1));
        scrollPane.setPreferredSize(new Dimension(300, 400));
        scrollPane.getViewport().setBackground(DOROTHY_WHITE);

        // Create button panel with fixed button colors
        JPanel buttonPanel = createButtonPanel();

        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBackground(Color.WHITE);

        JMenuItem addItem = new JMenuItem("Add Chunk");
        JMenuItem duplicateItem = new JMenuItem("Duplicate");
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem encryptItem = new JMenuItem("Encrypt");
        JMenuItem decryptItem = new JMenuItem("Decrypt");

        addItem.addActionListener(e -> addChunk());
        duplicateItem.addActionListener(e -> duplicateChunk());
        deleteItem.addActionListener(e -> deleteChunk());
        encryptItem.addActionListener(e -> encryptSelectedChunk());
        decryptItem.addActionListener(e -> decryptSelectedChunk());

        popupMenu.add(addItem);
        popupMenu.add(duplicateItem);
        popupMenu.addSeparator();
        popupMenu.add(encryptItem);
        popupMenu.add(decryptItem);
        popupMenu.addSeparator();
        popupMenu.add(deleteItem);

        return popupMenu;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(DOROTHY_WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JButton addButton = createStyledButton("Add", DOROTHY_PINK);
        JButton deleteButton = createStyledButton("Delete", DOROTHY_PURPLE);
        JButton clearButton = createStyledButton("Clear All", DOROTHY_DARK_PINK);

        addButton.addActionListener(e -> addChunk());
        deleteButton.addActionListener(e -> deleteChunk());
        clearButton.addActionListener(e -> clearAllChunks());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = renderButton(text, bgColor);
        button.setPreferredSize(new Dimension(90, 30));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    public void updateChunkList(List<TextChunk> chunks) {
        chunkListModel.clear();

        for (int i = 0; i < chunks.size(); i++) {
            TextChunk chunk = chunks.get(i);
            String displayText = String.format("[%d] ", i + 1);

            if (chunk.isEncrypted()) {
                displayText += "[ENCRYPTED] ";
                displayText += chunk.getDisplayText(20);
            } else {
                displayText += chunk.getDisplayText(40);
            }

            chunkListModel.addElement(displayText);
        }

        if (!chunks.isEmpty()) {
            chunkList.setSelectedIndex(0);
            loadSelectedChunk();
        }
    }

    private void loadSelectedChunk() {
        int index = chunkList.getSelectedIndex();
        List<TextChunk> chunks = parent.getTextChunks();

        if (index >= 0 && index < chunks.size()) {
            TextChunk chunk = chunks.get(index);
            parent.getTextEditorPanel().loadChunk(chunk, index);
        }
    }

    public void addChunk() {
        // Create custom dialog with better styling
        JDialog dialog = new JDialog(parent, "Add Text Chunk", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Keyword panel
        JPanel keywordPanel = new JPanel(new BorderLayout(10, 5));
        keywordPanel.setBackground(Color.WHITE);

        JLabel keywordLabel = new JLabel("Keyword:");
        keywordLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        keywordLabel.setForeground(new Color(60, 60, 60));

        JTextField keywordField = new JTextField("Comment");
        keywordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        keywordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DOROTHY_LIGHT_PINK, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        keywordField.setBackground(new Color(255, 250, 252));

        keywordPanel.add(keywordLabel, BorderLayout.NORTH);
        keywordPanel.add(keywordField, BorderLayout.CENTER);

        // Text panel
        JPanel textPanel = new JPanel(new BorderLayout(10, 5));
        textPanel.setBackground(Color.WHITE);

        JLabel textLabel = new JLabel("Text:");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(new Color(60, 60, 60));

        JTextArea textArea = new JTextArea(6, 30);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(DOROTHY_LIGHT_PINK, 2));
        scrollPane.setBackground(new Color(255, 250, 252));

        textPanel.add(textLabel, BorderLayout.NORTH);
        textPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton okButton = createDialogButton("OK", DOROTHY_PINK);
        JButton cancelButton = createDialogButton("Cancel", new Color(150, 150, 150));

        okButton.addActionListener(e -> {
            String keyword = keywordField.getText().trim();
            String text = textArea.getText();

            if (!text.isEmpty()) {
                if (keyword.isEmpty()) {
                    keyword = "Comment";
                }

                TextChunk newChunk = new TextChunk(keyword, text, false);
                List<TextChunk> chunks = parent.getTextChunks();
                chunks.add(newChunk);

                updateChunkList(chunks);
                chunkList.setSelectedIndex(chunks.size() - 1);
                parent.log("Added new text chunk");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter some text for the chunk.",
                        "Empty Text",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(keywordPanel, BorderLayout.NORTH);
        mainPanel.add(textPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JButton createDialogButton(String text, Color bgColor) {
        JButton button = renderButton(text, bgColor);
        button.setPreferredSize(new Dimension(90, 32));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    public void deleteChunk() {
        int index = chunkList.getSelectedIndex();
        List<TextChunk> chunks = parent.getTextChunks();

        if (index >= 0 && index < chunks.size()) {
            int result = JOptionPane.showConfirmDialog(parent,
                    "Delete chunk " + (index + 1) + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                chunks.remove(index);
                updateChunkList(chunks);

                if (!chunks.isEmpty()) {
                    chunkList.setSelectedIndex(Math.min(index, chunks.size() - 1));
                } else {
                    parent.getTextEditorPanel().clearEditor();
                }

                parent.log("Deleted chunk " + (index + 1));
            }
        }
    }

    private void duplicateChunk() {
        int index = chunkList.getSelectedIndex();
        List<TextChunk> chunks = parent.getTextChunks();

        if (index >= 0 && index < chunks.size()) {
            TextChunk original = chunks.get(index);
            TextChunk duplicate = new TextChunk(
                    original.getKeyword(),
                    original.getText(),
                    original.isEncrypted()
            );

            chunks.add(index + 1, duplicate);
            updateChunkList(chunks);
            chunkList.setSelectedIndex(index + 1);

            parent.log("Duplicated chunk " + (index + 1));
        }
    }

    public void clearAllChunks() {
        List<TextChunk> chunks = parent.getTextChunks();

        if (!chunks.isEmpty()) {
            int result = JOptionPane.showConfirmDialog(parent,
                    "Clear all " + chunks.size() + " chunk(s)?",
                    "Confirm Clear All",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                chunks.clear();
                updateChunkList(chunks);
                parent.getTextEditorPanel().clearEditor();
                parent.log("Cleared all text chunks");
            }
        }
    }

    private void encryptSelectedChunk() {
        int index = chunkList.getSelectedIndex();
        List<TextChunk> chunks = parent.getTextChunks();

        if (index >= 0 && index < chunks.size()) {
            TextChunk chunk = chunks.get(index);
            if (!chunk.isEncrypted()) {
                try {
                    String encrypted = parent.getRSAManager().encrypt(chunk.getText());
                    chunk.setText(encrypted);
                    chunk.setEncrypted(true);
                    updateChunkList(chunks);
                    parent.log("Encrypted chunk " + (index + 1));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(parent,
                            "Encryption failed: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void decryptSelectedChunk() {
        int index = chunkList.getSelectedIndex();
        List<TextChunk> chunks = parent.getTextChunks();

        if (index >= 0 && index < chunks.size()) {
            TextChunk chunk = chunks.get(index);
            if (chunk.isEncrypted()) {
                try {
                    String decrypted = parent.getRSAManager().decrypt(chunk.getText());
                    chunk.setText(decrypted);
                    chunk.setEncrypted(false);
                    updateChunkList(chunks);
                    parent.log("Decrypted chunk " + (index + 1));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(parent,
                            "Decryption failed: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public int getSelectedIndex() {
        return chunkList.getSelectedIndex();
    }

    public void refreshDisplay() {
        updateChunkList(parent.getTextChunks());
    }
}