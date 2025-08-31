package doro;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DoroStyle {
    // Dorothy Theme Colors
    public static final Color DOROTHY_PINK = new Color(255, 182, 193);
    public static final Color DOROTHY_LIGHT_PINK = new Color(255, 218, 224);
    public static final Color DOROTHY_DARK_PINK = new Color(255, 105, 180);
    public static final Color DOROTHY_PURPLE = new Color(186, 146, 234);
    public static final Color DOROTHY_LIGHT_PURPLE = new Color(221, 196, 255);
    public static final Color DOROTHY_WHITE = new Color(255, 250, 252);
    
    // UI Colors
    public static final Color TEXT_PRIMARY = new Color(60, 60, 60);
    public static final Color TEXT_SECONDARY = new Color(120, 120, 120);
    public static final Color TEXT_DISABLED = new Color(180, 180, 180);
    public static final Color BORDER_LIGHT = new Color(230, 230, 230);
    public static final Color BORDER_DEFAULT = new Color(200, 200, 200);
    public static final Color BORDER_PINK = new Color(255, 200, 210);
    public static final Color BACKGROUND_LIGHT = new Color(250, 250, 250);
    public static final Color BACKGROUND_LIGHTER = new Color(245, 245, 245);
    public static final Color SUCCESS_GREEN = new Color(76, 175, 80);
    public static final Color ERROR_RED = new Color(220, 50, 50);
    public static final Color WARNING_ORANGE = new Color(255, 140, 0);
    
    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_DEFAULT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_LARGE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_ITALIC = new Font("Segoe UI", Font.ITALIC, 11);
    public static final Font FONT_CODE = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_LOGO = new Font("Segoe Script", Font.BOLD, 20);
    
    // Common Dimensions
    public static final Dimension BUTTON_SIZE = new Dimension(100, 35);
    public static final Dimension BUTTON_SIZE_SMALL = new Dimension(90, 30);
    public static final Dimension BUTTON_SIZE_LARGE = new Dimension(120, 35);
    
    // Padding Constants
    public static final int PADDING_SMALL = 5;
    public static final int PADDING_MEDIUM = 10;
    public static final int PADDING_LARGE = 20;
    
    // Border Radius
    public static final int BORDER_RADIUS = 10;
    public static final int BORDER_RADIUS_SMALL = 8;

    // Button Factory Methods
    public static JButton createButton(String text, Color bgColor) {
        return createButton(text, bgColor, BUTTON_SIZE, FONT_LARGE);
    }
    
    public static JButton createSmallButton(String text, Color bgColor) {
        return createButton(text, bgColor, BUTTON_SIZE_SMALL, FONT_DEFAULT);
    }
    
    private static JButton createButton(String text, Color bgColor, Dimension size, Font font) {
        JButton button = renderButton(text, bgColor);
        button.setPreferredSize(size);
        button.setFont(font);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private static JButton renderButton(String text, Color bgColor) {
        return new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button background
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);

                // Button text - WHITE for better visibility
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
    }
    
    // Border Factory Methods
    public static TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(DOROTHY_LIGHT_PINK, 2),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TITLE,
            TEXT_PRIMARY
        );
    }
    
    public static TitledBorder createSubtitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_DEFAULT,
            TEXT_SECONDARY
        );
    }
    
    // Text Component Factory Methods
    public static JTextField createTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(FONT_DEFAULT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DEFAULT, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }
    
    public static JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(FONT_CODE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        area.setBackground(BACKGROUND_LIGHT);
        return area;
    }
    
    // Panel Factory Methods
    public static JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    public static JPanel createGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, Color.WHITE,
                    0, getHeight(), DOROTHY_LIGHT_PINK
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }
    
    // ScrollPane Factory Method
    public static JScrollPane createScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_DEFAULT, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }
    
    // Common Insets
    public static Insets getDefaultInsets() {
        return new Insets(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM);
    }
    
    public static Insets getSmallInsets() {
        return new Insets(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL);
    }
    
    public static Insets getLargeInsets() {
        return new Insets(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE);
    }
}
