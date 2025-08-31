package ui;

import doro.DoroStyle;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static doro.DoroStyle.*;

public class ImagePreviewPanel extends JPanel {
    private final DoroFrame parent;
    private JLabel imageLabel;
    private JLabel infoLabel;
    private BufferedImage currentImage;
    private double zoomLevel = 1.0;

    public ImagePreviewPanel(DoroFrame parent) {
        this.parent = parent;
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(DORO_WHITE);

        // Control panel with better button styling
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, DoroStyle.PADDING_MEDIUM, DoroStyle.PADDING_MEDIUM));
        controlPanel.setBackground(DORO_LIGHT_PINK);
        Insets insets = DoroStyle.getDefaultInsets();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));

        JButton zoomInButton = DoroStyle.createButton("Zoom In (+)", DORO_PURPLE);
        JButton zoomOutButton = DoroStyle.createButton("Zoom Out (-)", DORO_PURPLE);
        JButton fitButton = DoroStyle.createButton("Fit to Window", DORO_DARK_PINK);
        JButton actualSizeButton = DoroStyle.createButton("Actual Size", DORO_DARK_PINK);

        zoomInButton.addActionListener(e -> zoom(1.25));
        zoomOutButton.addActionListener(e -> zoom(0.8));
        fitButton.addActionListener(e -> fitToWindow());
        actualSizeButton.addActionListener(e -> actualSize());

        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(fitButton);
        controlPanel.add(actualSizeButton);

        // Image display area
        imageLabel = new JLabel("Drop a PNG file here or use File → Open", SwingConstants.CENTER);
        imageLabel.setFont(DoroStyle.FONT_ITALIC);
        imageLabel.setForeground(DoroStyle.TEXT_SECONDARY);

        JScrollPane scrollPane = DoroStyle.createScrollPane(imageLabel);
        scrollPane.setBackground(DORO_WHITE);
        scrollPane.getViewport().setBackground(DORO_WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(DORO_LIGHT_PINK, 1));

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(DORO_WHITE);
        infoPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, DORO_LIGHT_PINK));

        infoLabel = new JLabel("No image loaded", SwingConstants.CENTER);
        infoLabel.setFont(DoroStyle.FONT_DEFAULT);
        infoLabel.setForeground(DORO_PURPLE);

        infoPanel.add(infoLabel);

        // Add components
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }


    public void loadImage(File file) {
        try {
            currentImage = ImageIO.read(file);
            if (currentImage != null) {
                fitToWindow();
                updateInfo();
            }
        } catch (Exception e) {
            parent.log("Error loading image preview: " + e.getMessage());
        }
    }

    private void updateDisplay() {
        if (currentImage == null) return;

        int width = (int)(currentImage.getWidth() * zoomLevel);
        int height = (int)(currentImage.getHeight() * zoomLevel);

        Image scaledImage = currentImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaledImage));
        imageLabel.setText("");
    }

    private void updateInfo() {
        if (currentImage != null) {
            String info = String.format("Image: %dx%d pixels | Zoom: %.0f%%",
                    currentImage.getWidth(),
                    currentImage.getHeight(),
                    zoomLevel * 100);
            infoLabel.setText(info);
        }
    }

    private void zoom(double factor) {
        if (currentImage == null) return;

        zoomLevel *= factor;
        zoomLevel = Math.max(0.1, Math.min(5.0, zoomLevel)); // Limit zoom between 10% and 500%

        updateDisplay();
        updateInfo();
    }

    private void fitToWindow() {
        if (currentImage == null) return;

        int panelWidth = getWidth() - 50;
        int panelHeight = getHeight() - 150;

        double widthRatio = (double)panelWidth / currentImage.getWidth();
        double heightRatio = (double)panelHeight / currentImage.getHeight();

        zoomLevel = Math.min(widthRatio, heightRatio);
        zoomLevel = Math.min(zoomLevel, 1.0); // Don't zoom in beyond 100%

        updateDisplay();
        updateInfo();
    }

    private void actualSize() {
        zoomLevel = 1.0;
        updateDisplay();
        updateInfo();
    }
}