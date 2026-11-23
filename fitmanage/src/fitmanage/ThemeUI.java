package fitmanage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class ThemeUI {
    public static final Color BG_MAIN = new Color(18, 18, 18);
    public static final Color BG_NAV = new Color(24, 24, 24);
    public static final Color BG_CARD = new Color(30, 30, 30);
    public static final Color BG_INPUT = new Color(38, 38, 38);
    public static final Color BORDER_CARD = new Color(48, 48, 48);
    public static final Color BG_TABLE_HEADER = new Color(38, 38, 38);

    public static final Color ACCENT_ORANGE = new Color(242, 140, 40);
    public static final Color ACCENT_YELLOW = new Color(235, 180, 20);
    public static final Color BAR_SLATE = new Color(78, 104, 130);
    public static final Color DONUT_RED = new Color(215, 48, 48);
    public static final Color DONUT_GRAY = new Color(75, 75, 75);

    public static final Color TEXT_WHITE = new Color(245, 245, 245);
    public static final Color TEXT_GRAY = new Color(155, 155, 155);
    public static final Color TEXT_GREEN = new Color(74, 222, 128);
    public static final Color BADGE_GREEN_BG = new Color(24, 58, 40);

    public static class RoundedButton extends JButton {
        private final Color bgColor;
        private final Color borderColor;

        public RoundedButton(String text, Color bgColor, Color fgColor, Color borderColor) {
            super(text);
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            setForeground(fgColor);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 16, 9, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getModel().isRollover() ? bgColor.brighter() : bgColor;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class TintedLogoPanel extends JComponent {
        private BufferedImage logoImage;

        public TintedLogoPanel(int targetSize) {
            setPreferredSize(new Dimension(targetSize, targetSize));
            String[] candidates = {"erasebg-transformed.png", "logo_fitManage.png", "logo_FitManage.png"};
            for (String name : candidates) {
                BufferedImage raw = loadLogoImage(name);
                if (raw != null) {
                    logoImage = prepareCleanLogo(raw, targetSize);
                    break;
                }
            }
        }

        private BufferedImage loadLogoImage(String fileName) {
            try {
                InputStream is = ThemeUI.class.getResourceAsStream("/" + fileName);
                if (is != null) return ImageIO.read(is);

                InputStream pkgStream = ThemeUI.class.getResourceAsStream("/fitmanage/" + fileName);
                if (pkgStream != null) return ImageIO.read(pkgStream);

                String[] paths = {fileName, "src/" + fileName, "src/fitmanage/" + fileName};
                for (String p : paths) {
                    File f = new File(p);
                    if (f.exists() && f.isFile()) return ImageIO.read(f);
                }
            } catch (Exception ignored) {}
            return null;
        }

        private BufferedImage prepareCleanLogo(BufferedImage source, int size) {
            BufferedImage cleaned = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    int argb = source.getRGB(x, y);
                    int alpha = (argb >> 24) & 0xff;
                    int r = (argb >> 16) & 0xff;
                    int g = (argb >> 8) & 0xff;
                    int b = argb & 0xff;
                    if (alpha <= 5 || (r > 240 && g > 240 && b > 240)) {
                        cleaned.setRGB(x, y, 0x00000000);
                    } else {
                        cleaned.setRGB(x, y, argb);
                    }
                }
            }
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2s = scaled.createGraphics();
            g2s.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2s.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2s.drawImage(cleaned, 0, 0, size, size, null);
            g2s.dispose();
            return scaled;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (logoImage != null) {
                int x = (getWidth() - logoImage.getWidth()) / 2;
                int y = (getHeight() - logoImage.getHeight()) / 2;
                g2.drawImage(logoImage, x, y, null);
            } else {
                // Fallback icon oranye
                g2.setColor(ACCENT_ORANGE);
                int[] xTop = {4, 24, 20, 4};
                int[] yTop = {4, 4, 10, 10};
                g2.fillPolygon(xTop, yTop, 4);
                int[] xMid = {4, 18, 15, 4};
                int[] yMid = {12, 12, 18, 18};
                g2.fillPolygon(xMid, yMid, 4);
                g2.fillRect(4, 4, 6, 22);
            }
            g2.dispose();
        }
    }

    public static class AvatarCircle extends JComponent {
        private final String letter;
        private final Color bg;

        public AvatarCircle(String letter, Color bg) {
            this.letter = letter;
            this.bg = bg;
            setPreferredSize(new Dimension(36, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(letter)) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(letter, x, y);
            g2.dispose();
        }
    }

    public enum IconType { USER, CALENDAR, DUMBBELL, COINS }

    public static class MetricIcon extends JComponent {
        private final IconType type;

        public MetricIcon(IconType type) {
            this.type = type;
            setPreferredSize(new Dimension(22, 22));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ACCENT_ORANGE);

            switch (type) {
                case USER:
                    g2.fillOval(7, 2, 8, 8);
                    g2.fillArc(3, 11, 16, 13, 0, 180);
                    break;
                case CALENDAR:
                    g2.fillRoundRect(3, 3, 16, 15, 3, 3);
                    g2.setColor(BG_CARD);
                    g2.fillRect(5, 8, 12, 8);
                    g2.setColor(ACCENT_ORANGE);
                    g2.fillRect(6, 2, 2, 3);
                    g2.fillRect(14, 2, 2, 3);
                    break;
                case DUMBBELL:
                    g2.fillRoundRect(2, 7, 3, 8, 2, 2);
                    g2.fillRoundRect(5, 4, 3, 14, 2, 2);
                    g2.fillRect(8, 10, 6, 3);
                    g2.fillRoundRect(14, 4, 3, 14, 2, 2);
                    g2.fillRoundRect(17, 7, 3, 8, 2, 2);
                    break;
                case COINS:
                    g2.fillOval(2, 3, 12, 5);
                    g2.fillRect(2, 6, 12, 3);
                    g2.fillRect(2, 11, 12, 3);
                    g2.fillOval(9, 9, 11, 11);
                    g2.setColor(BG_CARD);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    g2.drawString("$", 12, 18);
                    break;
            }
            g2.dispose();
        }
    }

    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
            cell.setBackground(isSelected ? new Color(42, 42, 42) : BG_CARD);

            String text = value != null ? value.toString() : "";
            boolean isPositive = text.equalsIgnoreCase("Aktif") || text.equalsIgnoreCase("Lunas") || text.equalsIgnoreCase("Tersedia");

            JLabel badge = new JLabel(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isPositive ? BADGE_GREEN_BG : new Color(68, 52, 24));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setForeground(isPositive ? TEXT_GREEN : ACCENT_YELLOW);
            badge.setFont(new Font("SansSerif", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(4, 12, 4, 12));
            cell.add(badge);
            return cell;
        }
    }

    public static void styleDarkTable(JTable table, int statusColumnIndex) {
        table.setBackground(BG_CARD);
        table.setForeground(new Color(225, 225, 225));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(45, 45, 45));
        table.setSelectionBackground(new Color(42, 42, 42));
        table.setSelectionForeground(TEXT_WHITE);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_TABLE_HEADER);
        header.setForeground(TEXT_WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CARD));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                lbl.setBackground(isSel ? new Color(42, 42, 42) : BG_CARD);
                lbl.setForeground(c == 0 ? TEXT_GRAY : new Color(225, 225, 225));
                return lbl;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == statusColumnIndex) {
                table.getColumnModel().getColumn(i).setCellRenderer(new StatusBadgeRenderer());
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
            }
        }
    }

    public static JScrollPane wrapDarkScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBackground(BG_CARD);
        return scroll;
    }

    public static JTextField createDarkTextField(String initialText) {
        JTextField tf = new JTextField(initialText);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(TEXT_WHITE);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return tf;
    }

    public static <T> JComboBox<T> createDarkComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_WHITE);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setBorder(BorderFactory.createLineBorder(BORDER_CARD, 1, true));
        return cb;
    }
}