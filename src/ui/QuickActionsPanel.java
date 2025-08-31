package ui;

import doro.DoroStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class QuickActionsPanel extends JPanel {
    private DoroFrame parent;
    private JLabel fileInfoLabel;
    private JButton openButton;
    private JButton saveButton;
    private JButton addChunkButton;
    private JButton encryptAllButton;
    private JButton decryptAllButton;

    // Modern color scheme
    private static final Color BG_COLOR = new Color(250, 250, 250);
    private static final Color ACCENT_PINK = new Color(255, 107, 129);
    private static final Color ACCENT_PURPLE = new Color(156, 89, 182);
    private static final Color ACCENT_BLUE = new Color(66, 165, 245);
    private static final Color TEXT_PRIMARY = new Color(60, 60, 60);
    private static final Color TEXT_SECONDARY = new Color(120, 120, 120);

    public QuickActionsPanel(DoroFrame parent) {
        this.parent = parent;
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Left side - Logo and file info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("Dorothy");
        logoLabel.setFont(new Font("Segoe Script", Font.BOLD, 20));
        logoLabel.setForeground(ACCENT_PINK);

        fileInfoLabel = new JLabel("No file loaded");
        fileInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fileInfoLabel.setForeground(TEXT_SECONDARY);

        leftPanel.add(logoLabel);
        leftPanel.add(new JSeparator(SwingConstants.VERTICAL));
        leftPanel.add(fileInfoLabel);

        // Center - Quick actions
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        centerPanel.setOpaque(false);

        openButton = createActionButton("Open", ACCENT_BLUE);
        saveButton = createActionButton("Save", ACCENT_BLUE);
        addChunkButton = createActionButton("+ Add Text", ACCENT_PURPLE);
        encryptAllButton = createActionButton("Encrypt All", ACCENT_PINK);
        decryptAllButton = createActionButton("Decrypt All", ACCENT_PURPLE);

        // Add action listeners
        openButton.addActionListener(e -> parent.openFile());
        saveButton.addActionListener(e -> parent.saveFile());
        addChunkButton.addActionListener(e -> parent.getChunkPanel().addChunk());
        encryptAllButton.addActionListener(e -> parent.encryptAllChunks());
        decryptAllButton.addActionListener(e -> parent.decryptAllChunks());

        // Add tooltips
        openButton.setToolTipText("Open PNG file (Ctrl+O)");
        saveButton.setToolTipText("Save changes (Ctrl+S)");
        addChunkButton.setToolTipText("Add new text chunk");
        encryptAllButton.setToolTipText("Encrypt all text chunks");
        decryptAllButton.setToolTipText("Decrypt all text chunks");

        centerPanel.add(openButton);
        centerPanel.add(saveButton);
        centerPanel.add(createSeparator());
        centerPanel.add(addChunkButton);
        centerPanel.add(createSeparator());
        centerPanel.add(encryptAllButton);
        centerPanel.add(decryptAllButton);

        // Right side - Status
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JLabel keyStatusLabel = new JLabel("Keys Ready");
        keyStatusLabel.setForeground(new Color(76, 175, 80));
        keyStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel keyIcon = new JLabel("\u2713"); // Checkmark
        keyIcon.setForeground(new Color(76, 175, 80));
        keyIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));

        rightPanel.add(keyIcon);
        rightPanel.add(keyStatusLabel);

        // Add all panels
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = DoroStyle.renderButton(text,bgColor);

        button.setPreferredSize(new Dimension(110, 32));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }

    private Component createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 20));
        separator.setForeground(new Color(230, 230, 230));
        return separator;
    }

    public void updateFileInfo(File file) {
        if (file != null) {
            String name = file.getName();
            long size = file.length();
            String sizeStr = formatFileSize(size);
            fileInfoLabel.setText(name + " (" + sizeStr + ")");
            fileInfoLabel.setForeground(TEXT_PRIMARY);
            fileInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        } else {
            fileInfoLabel.setText("No file loaded");
            fileInfoLabel.setForeground(TEXT_SECONDARY);
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}