package fitmanage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Panel kartu gelap: isi #1C1C1F, border halus #2C2C30, sudut membulat.
 * Dipakai untuk kartu KPI, kartu chart, kartu tabel, dan semua panel sub-halaman.
 */
public class CardPanel extends JPanel {

    private final int radius;
    private Color fill;
    private Color border;

    public CardPanel() {
        this(Theme.ARC, Theme.CARD, Theme.BORDER);
    }

    public CardPanel(int radius) {
        this(radius, Theme.CARD, Theme.BORDER);
    }

    public CardPanel(int radius, Color fill, Color border) {
        super(new BorderLayout());
        this.radius = radius;
        this.fill = fill;
        this.border = border;
        setOpaque(false);
    }

    public void setFill(Color c) {
        this.fill = c;
        repaint();
    }

    public void setBorderColor(Color c) {
        this.border = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int d = radius * 2;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, d, d);
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, d, d);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
