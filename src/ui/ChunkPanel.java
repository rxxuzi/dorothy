package ui;

import doro.DoroStyle;
import model.TextChunk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static doro.DoroStyle.*;

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
        setBackground(DORO_WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                DoroStyle.createTitledBorder("Text Chunks"),
                BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL)
        ));

        // Create list with custom renderer
        chunkListModel = new DefaultListModel<>();
        chunkList = new JList<>(chunkListModel);
        chunkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chunkList.setBackground(DORO_WHITE);
        chunkList.setSelectionBackground(DORO_PURPLE);
        chunkList.setSelectionForeground(Color.WHITE);
        chunkList.setFont(FONT_DEFAULT);

        chunkList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                label.setBorder(BorderFactory.createEmptyBorder(8, PADDING_MEDIUM, 8, PADDING_MEDIUM));

                if (isSelected) {
                    label.setBackground(DORO_PURPLE);
                    label.setForeground(Color.WHITE);
                    label.setFont(FONT_SUBTITLE);
                } else {
                    label.setBackground(DORO_WHITE);
                    if (value.toString().contains("[ENCRYPTED]")) {
                        label.setForeground(ERROR_RED);
                        label.setFont(FONT_ITALIC);
                    } else {
                        label.setForeground(TEXT_PRIMARY);
                        label.setFont(FONT_DEFAULT);
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

        JScrollPane scrollPane = DoroStyle.createScrollPane(chunkList);
        scrollPane.setBorder(BorderFactory.createLineBorder(DORO_LIGHT_PINK, 1));
        scrollPane.setPreferredSize(new Dimension(300, 400));
        scrollPane.getViewport().setBackground(DORO_WHITE);

        // Create button panel with fixed button colors
        JPanel buttonPanel = createButtonPanel();

        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBackground(DORO_WHITE);

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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, PADDING_MEDIUM, PADDING_MEDIUM));
        buttonPanel.setBackground(DORO_WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_SMALL, 0, PADDING_SMALL, 0));

        JButton addButton = DoroStyle.createSmallButton("Add", DORO_PINK);
        JButton deleteButton = DoroStyle.createSmallButton("Delete", DORO_PURPLE);
        JButton clearButton = DoroStyle.createSmallButton("Clear All", DORO_DARK_PINK);

        addButton.addActionListener(e -> addChunk());
        deleteButton.addActionListener(e -> deleteChunk());
        clearButton.addActionListener(e -> clearAllChunks());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        return buttonPanel;
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

        JPanel mainPanel = new JPanel(new BorderLayout(PADDING_MEDIUM, PADDING_MEDIUM));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE));

        // Keyword panel
        JPanel keywordPanel = new JPanel(new BorderLayout(PADDING_MEDIUM, PADDING_SMALL));
        keywordPanel.setBackground(Color.WHITE);

        JLabel keywordLabel = new JLabel("Keyword:");
        keywordLabel.setFont(FONT_TITLE);
        keywordLabel.setForeground(TEXT_PRIMARY);

        JTextField keywordField = DoroStyle.createTextField("Comment");
        keywordField.setFont(FONT_LARGE);
        keywordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DORO_LIGHT_PINK, 2),
                BorderFactory.createEmptyBorder(8, PADDING_MEDIUM, 8, PADDING_MEDIUM)
        ));
        keywordField.setBackground(DORO_WHITE);

        keywordPanel.add(keywordLabel, BorderLayout.NORTH);
        keywordPanel.add(keywordField, BorderLayout.CENTER);

        // Text panel
        JPanel textPanel = new JPanel(new BorderLayout(PADDING_MEDIUM, PADDING_SMALL));
        textPanel.setBackground(Color.WHITE);

        JLabel textLabel = new JLabel("Text:");
        textLabel.setFont(FONT_TITLE);
        textLabel.setForeground(TEXT_PRIMARY);

        JTextArea textArea = DoroStyle.createTextArea();
        textArea.setRows(6);
        textArea.setColumns(30);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(DORO_LIGHT_PINK, 2));
        scrollPane.setBackground(DORO_WHITE);

        textPanel.add(textLabel, BorderLayout.NORTH);
        textPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_MEDIUM, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton okButton = DoroStyle.createSmallButton("OK", DORO_PINK);
        JButton cancelButton = DoroStyle.createSmallButton("Cancel", TEXT_DISABLED);

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