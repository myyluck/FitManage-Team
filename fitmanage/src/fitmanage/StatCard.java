package fitmanage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Kartu KPI: judul, angka besar (otomatis mengecil bila kepanjangan), teks keterangan,
 * dan ikon oranye di pojok kanan atas.
 */
public class StatCard extends CardPanel {

    private final FitLabel lblValue;
    private final JLabel lblSub;

    public StatCard(String judul, UiIcons.Type ikon) {
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel lblTitle = Theme.label(judul, Font.PLAIN, 15, Theme.TEXT);
        lblValue = new FitLabel("-", 18);
        lblValue.setFont(Theme.font(Font.BOLD, 32));
        lblValue.setForeground(Theme.TEXT);
        lblSub = Theme.label(" ", Font.PLAIN, 13, Theme.TEXT_DIM);

        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);
        lblValue.setAlignmentX(LEFT_ALIGNMENT);
        lblSub.setAlignmentX(LEFT_ALIGNMENT);
        teks.add(lblTitle);
        teks.add(Box.createVerticalStrut(6));
        teks.add(lblValue);
        teks.add(Box.createVerticalStrut(6));
        teks.add(lblSub);

        JPanel ikonWrap = new JPanel(new BorderLayout());
        ikonWrap.setOpaque(false);
        ikonWrap.add(new IconBadge(ikon), BorderLayout.NORTH);

        add(teks, BorderLayout.CENTER);
        add(ikonWrap, BorderLayout.EAST);
    }

    public void setValue(String nilai) {
        lblValue.setText(nilai);
    }

    /** Boleh teks biasa atau HTML, mis. "Naik <font color='#4ADE80'>29%</font> dari kemarin". */
    public void setSub(String teks) {
        lblSub.setText(teks == null || teks.isEmpty() ? " " : teks);
    }

    // ---------------------------------------------------------------
    private static class IconBadge extends JComponent {
        private final UiIcons.Type tipe;

        IconBadge(UiIcons.Type tipe) {
            this.tipe = tipe;
            setPreferredSize(new Dimension(34, 34));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UiIcons.paint(g2, tipe, 2, 2, 30, Theme.ORANGE);
            g2.dispose();
        }
    }

    /** Label yang mengecilkan font sendiri supaya angka panjang (Rp ...) tidak terpotong. */
    private static class FitLabel extends JLabel {
        private final float minSize;

        FitLabel(String text, float minSize) {
            super(text);
            this.minSize = minSize;
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(0, super.getPreferredSize().height);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            String text = getText();
            if (text == null || text.isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Insets in = getInsets();
            int tersedia = getWidth() - in.left - in.right;
            Font font = getFont();
            float ukuran = font.getSize2D();
            FontMetrics fm = g2.getFontMetrics(font);
            while (ukuran > minSize && fm.stringWidth(text) > tersedia) {
                ukuran -= 1f;
                font = font.deriveFont(ukuran);
                fm = g2.getFontMetrics(font);
            }
            g2.setFont(font);
            g2.setColor(getForeground());
            int y = in.top + (getHeight() - in.top - in.bottom - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, in.left, y);
            g2.dispose();
        }
    }
}
