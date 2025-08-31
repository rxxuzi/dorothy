package ui;

import doro.DoroStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class QuickActionsPanel extends JPanel {
    private final DoroFrame parent;
    private JLabel fileInfoLabel;

    // Using DoroStyle color constants

    public QuickActionsPanel(DoroFrame parent) {
        this.parent = parent;
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(DoroStyle.BACKGROUND_LIGHT);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, DoroStyle.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(DoroStyle.PADDING_LARGE, DoroStyle.PADDING_LARGE, DoroStyle.PADDING_LARGE, DoroStyle.PADDING_LARGE)
        ));

        // Left side - Logo and file info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("Dorothy");
        logoLabel.setFont(DoroStyle.FONT_LOGO);
        logoLabel.setForeground(DoroStyle.DORO_DARK_PINK);

        fileInfoLabel = new JLabel("No file loaded");
        fileInfoLabel.setFont(DoroStyle.FONT_LARGE);
        fileInfoLabel.setForeground(DoroStyle.TEXT_SECONDARY);

        leftPanel.add(logoLabel);
        leftPanel.add(new JSeparator(SwingConstants.VERTICAL));
        leftPanel.add(fileInfoLabel);

        // Center - Quick actions
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        centerPanel.setOpaque(false);

        JButton openButton = createActionButton("Open", DoroStyle.DORO_PURPLE);
        JButton saveButton = createActionButton("Save", DoroStyle.DORO_PURPLE);
        JButton addChunkButton = createActionButton("+ Add Text", DoroStyle.DORO_PINK);
        JButton encryptAllButton = createActionButton("Encrypt All", DoroStyle.DORO_DARK_PINK);
        JButton decryptAllButton = createActionButton("Decrypt All", DoroStyle.DORO_PINK);

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
        keyStatusLabel.setForeground(DoroStyle.SUCCESS_GREEN);
        keyStatusLabel.setFont(DoroStyle.FONT_DEFAULT);

        JLabel keyIcon = new JLabel("✓"); // Checkmark
        keyIcon.setForeground(DoroStyle.SUCCESS_GREEN);
        keyIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));

        rightPanel.add(keyIcon);
        rightPanel.add(keyStatusLabel);

        // Add all panels
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = DoroStyle.createButton(text, bgColor);
        button.setPreferredSize(new Dimension(110, 32));
        
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
        separator.setForeground(DoroStyle.BORDER_LIGHT);
        return separator;
    }

    public void updateFileInfo(File file) {
        if (file != null) {
            String name = file.getName();
            long size = file.length();
            String sizeStr = formatFileSize(size);
            fileInfoLabel.setText(name + " (" + sizeStr + ")");
            fileInfoLabel.setForeground(DoroStyle.TEXT_PRIMARY);
            fileInfoLabel.setFont(DoroStyle.FONT_LARGE);
        } else {
            fileInfoLabel.setText("No file loaded");
            fileInfoLabel.setForeground(DoroStyle.TEXT_SECONDARY);
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}