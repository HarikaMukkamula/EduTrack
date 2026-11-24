package com.edutrack.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UITheme: Shared styling, colors, fonts, and widget factories for EduTrack Swing GUI.
 */
public final class UITheme {
    // Professional University Color Palette
    public static final Color PRIMARY = new Color(26, 54, 110);        // Deep Navy (#1A366E)
    public static final Color PRIMARY_DARK = new Color(17, 37, 78);    // Midnight Navy
    public static final Color PRIMARY_LIGHT = new Color(238, 242, 255); // Soft Lavender
    public static final Color ACCENT = new Color(37, 99, 235);         // Royal Blue (#2563EB)
    public static final Color ACCENT_HOVER = new Color(29, 78, 216);
    
    public static final Color BG_LIGHT = new Color(248, 250, 252);     // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);       // Slate 200
    public static final Color BORDER_DARK = new Color(203, 213, 225);  // Slate 300
    
    public static final Color TEXT_DARK = new Color(15, 23, 42);       // Slate 900
    public static final Color TEXT_BODY = new Color(51, 65, 85);       // Slate 700
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Slate 500
    
    public static final Color SUCCESS = new Color(22, 163, 74);        // Emerald 600
    public static final Color SUCCESS_BG = new Color(240, 253, 244);
    public static final Color WARNING = new Color(217, 119, 6);        // Amber 600
    public static final Color WARNING_BG = new Color(254, 252, 232);
    public static final Color DANGER = new Color(220, 38, 38);         // Red 600
    public static final Color DANGER_BG = new Color(254, 242, 242);

    // Standardized Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {}

    /**
     * Creates a styled Primary action button (Navy/Royal Blue).
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(ACCENT_HOVER, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(ACCENT);
            }
        });
        return btn;
    }

    /**
     * Creates a styled Secondary/Outline button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_BODY);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER_DARK, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BG_LIGHT);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    /**
     * Creates a Danger/Delete button.
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(DANGER);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(185, 28, 28), 1),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    /**
     * Creates a Success button.
     */
    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(SUCCESS);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(21, 128, 61), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    /**
     * Creates a white elevated Card Panel with subtle border.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    /**
     * Formats and styles a JTable for modern university dashboard display.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setGridColor(BORDER);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(new LineBorder(BORDER, 1));

        // Default cell padding
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 252));
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }

    /**
     * Creates a top Navigation/Header Banner for role dashboards.
     */
    public static JPanel createHeaderBanner(String title, String subtitle, String userRole, String userName, Runnable onLogout) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(PRIMARY);
        banner.setBorder(new EmptyBorder(14, 24, 14, 24));

        // Left Side: Title & Subtitle
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("🎓 " + title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(new Color(203, 213, 225));

        left.add(lblTitle);
        left.add(lblSub);

        // Right Side: User info badge & Logout button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel lblUser = new JLabel("<html><div style='text-align: right;'><b>" + userName + "</b><br><span style='color: #93C5FD; font-size: 10px;'>" + userRole + "</span></div></html>");
        lblUser.setFont(FONT_BODY);
        lblUser.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout ⎋");
        btnLogout.setFont(FONT_BOLD);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 38, 38), 1),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });

        right.add(lblUser);
        right.add(btnLogout);

        banner.add(left, BorderLayout.WEST);
        banner.add(right, BorderLayout.EAST);
        return banner;
    }

    /**
     * Creates a modern metric KPI summary card.
     */
    public static JPanel createMetricCard(String title, String value, Color accentColor) {
        JPanel card = createCardPanel();
        card.setLayout(new GridLayout(2, 1, 0, 4));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(accentColor);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_SMALL);
        lblTitle.setForeground(TEXT_MUTED);

        card.add(lblVal);
        card.add(lblTitle);
        return card;
    }
}
