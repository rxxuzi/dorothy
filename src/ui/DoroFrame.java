package ui;

import model.*;
import cipher.*;
import png.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class DoroFrame extends JFrame {
    // UI Components
    private ChunkPanel chunkPanel;
    private TextEditorPanel textEditorPanel;
    private ImagePreviewPanel imagePreviewPanel;
    private QuickActionsPanel quickActionsPanel;
    private JTextArea logArea;

    // Data
    private File currentFile;
    private List<TextChunk> textChunks;
    private RSAManager rsaManager;
    private PNGProcessor pngProcessor;

    // Colors - Dorothy theme
    public static final Color DOROTHY_PINK = new Color(255, 182, 193);
    public static final Color DOROTHY_LIGHT_PINK = new Color(255, 218, 224);
    public static final Color DOROTHY_DARK_PINK = new Color(255, 105, 180);
    public static final Color DOROTHY_PURPLE = new Color(186, 146, 234);
    public static final Color DOROTHY_LIGHT_PURPLE = new Color(221, 196, 255);
    public static final Color DOROTHY_WHITE = new Color(255, 250, 252);

    public DoroFrame() {
        rsaManager = new RSAManager();
        pngProcessor = new PNGProcessor();
        textChunks = new ArrayList<>();

        initializeGUI();
        setupDragAndDrop();
        applyTheme();

        log("Dorothy started - RSA keys ready!");
    }

    private void initializeGUI() {
        setTitle("Dorothy - PNG Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Use custom icon if available
        try {
            setIconImage(createDorothyIcon());
        } catch (Exception e) {
            // Use default icon
        }

        // Main container with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(
                        0, 0, DOROTHY_WHITE,
                        0, getHeight(), DOROTHY_LIGHT_PINK
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setOpaque(false);

        // Create menu bar
        setJMenuBar(createMenuBar());

        // Top section - Quick actions
        quickActionsPanel = new QuickActionsPanel(this);
        mainPanel.add(quickActionsPanel, BorderLayout.NORTH);

        // Center section - Main workspace
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom section - Status and log
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Set frame properties
        setSize(1200, 800);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(DOROTHY_WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DOROTHY_LIGHT_PINK));

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        JMenuItem openItem = createMenuItem("Open PNG", KeyEvent.VK_O);
        JMenuItem saveItem = createMenuItem("Save", KeyEvent.VK_S);
        JMenuItem saveAsItem = createMenuItem("Save As...", KeyEvent.VK_A);
        JMenuItem exitItem = createMenuItem("Exit", KeyEvent.VK_X);

        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());
        saveAsItem.addActionListener(e -> saveFileAs());
        exitItem.addActionListener(e -> {
            if (confirmExit()) System.exit(0);
        });

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');

        JMenuItem addChunkItem = createMenuItem("Add Text Chunk", KeyEvent.VK_N);
        JMenuItem deleteChunkItem = createMenuItem("Delete Chunk", KeyEvent.VK_D);
        JMenuItem clearAllItem = createMenuItem("Clear All Chunks", 0);

        addChunkItem.addActionListener(e -> chunkPanel.addChunk());
        deleteChunkItem.addActionListener(e -> chunkPanel.deleteChunk());
        clearAllItem.addActionListener(e -> chunkPanel.clearAllChunks());

        editMenu.add(addChunkItem);
        editMenu.add(deleteChunkItem);
        editMenu.addSeparator();
        editMenu.add(clearAllItem);

        // Security menu
        JMenu securityMenu = new JMenu("Security");
        securityMenu.setMnemonic('S');

        JMenuItem generateKeysItem = createMenuItem("Generate New Keys", KeyEvent.VK_G);
        JMenuItem exportPublicItem = createMenuItem("Export Public Key", KeyEvent.VK_E);
        JMenuItem importPublicItem = createMenuItem("Import Public Key", KeyEvent.VK_I);
        JMenuItem encryptAllItem = createMenuItem("Encrypt All Chunks", 0);
        JMenuItem decryptAllItem = createMenuItem("Decrypt All Chunks", 0);

        generateKeysItem.addActionListener(e -> generateNewKeys());
        exportPublicItem.addActionListener(e -> exportPublicKey());
        importPublicItem.addActionListener(e -> importPublicKey());
        encryptAllItem.addActionListener(e -> encryptAllChunks());
        decryptAllItem.addActionListener(e -> decryptAllChunks());

        securityMenu.add(generateKeysItem);
        securityMenu.addSeparator();
        securityMenu.add(exportPublicItem);
        securityMenu.add(importPublicItem);
        securityMenu.addSeparator();
        securityMenu.add(encryptAllItem);
        securityMenu.add(decryptAllItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        JMenuItem aboutItem = createMenuItem("About Dorothy", KeyEvent.VK_F1);
        JMenuItem guideItem = createMenuItem("Quick Guide", 0);

        aboutItem.addActionListener(e -> showAbout());
        guideItem.addActionListener(e -> showQuickGuide());

        helpMenu.add(guideItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(securityMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JMenuItem createMenuItem(String text, int keyCode) {
        JMenuItem item = new JMenuItem(text);
        if (keyCode != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(keyCode, ActionEvent.CTRL_MASK));
        }
        return item;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Create tabbed pane for different views
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(DOROTHY_WHITE);
        tabbedPane.setForeground(DOROTHY_DARK_PINK);

        // Tab 1: Editor view (split pane)
        JSplitPane editorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editorSplitPane.setOpaque(false);

        chunkPanel = new ChunkPanel(this);
        textEditorPanel = new TextEditorPanel(this);

        editorSplitPane.setLeftComponent(chunkPanel);
        editorSplitPane.setRightComponent(textEditorPanel);
        editorSplitPane.setDividerLocation(350);
        editorSplitPane.setDividerSize(5);

        // Tab 2: Image preview
        imagePreviewPanel = new ImagePreviewPanel(this);

        tabbedPane.addTab("Editor", editorSplitPane);
        tabbedPane.addTab("Image Preview", imagePreviewPanel);

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // Log area with better styling
        logArea = new JTextArea(4, 0);
        logArea.setEditable(false);
        logArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logArea.setBackground(new Color(255, 250, 252));
        logArea.setForeground(new Color(80, 80, 80));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(DOROTHY_LIGHT_PINK, 1),
                "Activity Log",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11),
                new Color(120, 120, 120)
        ));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setBackground(new Color(255, 250, 252));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupDragAndDrop() {
        new DropTarget(this, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                }
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    if (!files.isEmpty()) {
                        File file = files.get(0);
                        if (file.getName().toLowerCase().endsWith(".png")) {
                            currentFile = file;
                            loadPNG();
                            quickActionsPanel.updateFileInfo(file);
                        } else {
                            log("Warning: Please drop a PNG file");
                        }
                    }
                } catch (Exception e) {
                    log("Error handling dropped file: " + e.getMessage());
                }
            }

            @Override public void dragOver(DropTargetDragEvent dtde) {}
            @Override public void dragExit(DropTargetEvent dte) {}
            @Override public void dropActionChanged(DropTargetDragEvent dtde) {}
        });
    }

    private void applyTheme() {
        try {
            UIManager.put("TabbedPane.selected", DOROTHY_LIGHT_PINK);
            UIManager.put("TabbedPane.contentAreaColor", DOROTHY_WHITE);
            UIManager.put("Button.background", DOROTHY_PINK);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.focus", DOROTHY_PURPLE);
            UIManager.put("TextField.background", DOROTHY_WHITE);
            UIManager.put("TextArea.background", DOROTHY_WHITE);
            UIManager.put("List.background", DOROTHY_WHITE);
            UIManager.put("List.selectionBackground", DOROTHY_LIGHT_PURPLE);
            UIManager.put("ScrollBar.thumb", DOROTHY_PINK);
        } catch (Exception e) {
            // Fallback to default theme
        }
    }

    private Image createDorothyIcon() {
        // Create a simple icon programmatically
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw a pink circle
        g2d.setColor(DOROTHY_PINK);
        g2d.fillOval(4, 4, 24, 24);

        // Draw a white "D"
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("D", 10, 22);

        g2d.dispose();
        return icon;
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    // File operations
    public void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            loadPNG();
            quickActionsPanel.updateFileInfo(currentFile);
        }
    }

    public void loadPNG() {
        if (currentFile == null) return;

        try {
            List<PNGChunk> chunks = pngProcessor.readPNGChunks(currentFile);
            textChunks = pngProcessor.extractTextChunks(chunks);

            chunkPanel.updateChunkList(textChunks);
            imagePreviewPanel.loadImage(currentFile);

            log("Loaded " + textChunks.size() + " chunks from " + currentFile.getName());
        } catch (Exception e) {
            log("Error loading PNG: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error loading PNG: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void saveFile() {
        if (currentFile == null) {
            saveFileAs();
            return;
        }

        try {
            List<PNGChunk> originalChunks = pngProcessor.readPNGChunks(currentFile);
            List<PNGChunk> newChunks = pngProcessor.buildPNGWithTextChunks(originalChunks, textChunks);
            pngProcessor.writePNGChunks(currentFile, newChunks);

            log("Saved " + textChunks.size() + " chunks to " + currentFile.getName());
        } catch (Exception e) {
            log("Error saving file: " + e.getMessage());
        }
    }

    public void saveFileAs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("output.png"));
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File oldFile = currentFile;
            currentFile = chooser.getSelectedFile();

            if (!currentFile.getName().toLowerCase().endsWith(".png")) {
                currentFile = new File(currentFile.getAbsolutePath() + ".png");
            }

            if (oldFile != null && !currentFile.equals(oldFile)) {
                try {
                    java.nio.file.Files.copy(oldFile.toPath(), currentFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log("Error copying file: " + e.getMessage());
                }
            }

            saveFile();
            quickActionsPanel.updateFileInfo(currentFile);
        }
    }

    // Security operations
    public void encryptAllChunks() {
        int count = 0;
        for (TextChunk chunk : textChunks) {
            if (!chunk.isEncrypted()) {
                try {
                    String encrypted = rsaManager.encrypt(chunk.getText());
                    chunk.setText(encrypted);
                    chunk.setEncrypted(true);
                    count++;
                } catch (Exception e) {
                    log("Failed to encrypt chunk: " + e.getMessage());
                }
            }
        }

        if (count > 0) {
            chunkPanel.refreshDisplay();
            log("Encrypted " + count + " chunks");
        } else {
            log("No chunks to encrypt");
        }
    }

    public void decryptAllChunks() {
        int count = 0;
        for (TextChunk chunk : textChunks) {
            if (chunk.isEncrypted()) {
                try {
                    String decrypted = rsaManager.decrypt(chunk.getText());
                    chunk.setText(decrypted);
                    chunk.setEncrypted(false);
                    count++;
                } catch (Exception e) {
                    log("Failed to decrypt chunk: " + e.getMessage());
                }
            }
        }

        if (count > 0) {
            chunkPanel.refreshDisplay();
            log("Decrypted " + count + " chunks");
        } else {
            log("No chunks to decrypt");
        }
    }

    private void generateNewKeys() {
        int result = JOptionPane.showConfirmDialog(this,
                "Generate new RSA key pair?\nThis will replace existing keys.",
                "Generate Keys",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            rsaManager.generateNewKeys();
            log("New RSA key pair generated");
        }
    }

    private void exportPublicKey() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("dorothy_public_export.pub"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                rsaManager.exportPublicKey(chooser.getSelectedFile());
                log("Public key exported");
            } catch (IOException e) {
                log("Export failed: " + e.getMessage());
            }
        }
    }

    private void importPublicKey() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Public Key files", "pub", "pem", "key"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                rsaManager.importPublicKey(chooser.getSelectedFile());
                log("Public key imported");
            } catch (Exception e) {
                log("Import failed: " + e.getMessage());
            }
        }
    }

    private boolean confirmExit() {
        if (textChunks != null && !textChunks.isEmpty()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Exit anyway?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            return result == JOptionPane.YES_OPTION;
        }
        return true;
    }

    private void showAbout() {
        JDialog dialog = new JDialog(this, "About Dorothy", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("<html><center><h2 style='color:#FF69B4'>Dorothy</h2></center></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel textLabel = new JLabel(
                "<html><center>" +
                        "<p style='margin-top:10px'>PNG Text Editor with RSA Encryption</p>" +
                        "<p style='margin-top:15px;color:#666'>Author: rxxuzi</p>" +
                        "<p style='color:#666'>License: AGPL-3.0</p>" +
                        "<p style='margin-top:10px;color:#999;font-size:10px'>Copyright © 2025</p>" +
                        "</center></html>",
                SwingConstants.CENTER
        );

        JButton okButton = new JButton("OK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor = DOROTHY_PINK;
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };

        okButton.setPreferredSize(new Dimension(80, 32));
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        okButton.setContentAreaFilled(false);
        okButton.setBorderPainted(false);
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(okButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(textLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showQuickGuide() {
        JDialog dialog = new JDialog(this, "Quick Guide", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("<html><h3 style='color:#FF69B4'>Quick Guide</h3></html>");

        JTextArea guideText = new JTextArea(
                "How to use Dorothy:\n\n" +
                        "1. Drag & Drop or Open a PNG file\n" +
                        "2. Add/Edit text chunks in the editor\n" +
                        "3. Encrypt sensitive data with RSA encryption\n" +
                        "4. Save your modified PNG file\n\n" +
                        "Tips:\n" +
                        "• RSA limit: ~245 bytes per chunk\n" +
                        "• Keep your private key safe!\n" +
                        "• Share public keys for secure communication\n" +
                        "• Use Ctrl+S to quickly save changes"
        );
        guideText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        guideText.setEditable(false);
        guideText.setBackground(new Color(250, 250, 250));
        guideText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        guideText.setLineWrap(true);
        guideText.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(guideText);
        scrollPane.setBorder(BorderFactory.createLineBorder(DOROTHY_LIGHT_PINK, 1));

        JButton okButton = new JButton("OK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor = DOROTHY_PURPLE;
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };

        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        okButton.setContentAreaFilled(false);
        okButton.setBorderPainted(false);
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(okButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    // Getters
    public List<TextChunk> getTextChunks() { return textChunks; }
    public RSAManager getRSAManager() { return rsaManager; }
    public ChunkPanel getChunkPanel() { return chunkPanel; }
    public TextEditorPanel getTextEditorPanel() { return textEditorPanel; }
    public File getCurrentFile() { return currentFile; }
}