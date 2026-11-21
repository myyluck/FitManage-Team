package fitmanage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.Locale;
import javax.swing.JComponent;

/**
 * Donut/Ring chart komposisi check-in (Reguler = merah, VIP = abu gelap).
 * Cincin di kiri dengan total di tengah, legenda vertikal di kanan.
 */
public class DonutChart extends JComponent {

    private static final Locale ID = Locale.forLanguageTag("id-ID");

    private int reguler;
    private int vip;

    public DonutChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(320, 220));
    }

    public void setData(int reguler, int vip) {
        this.reguler = Math.max(0, reguler);
        this.vip = Math.max(0, vip);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth();
        int h = getHeight();
        int total = reguler + vip;

        int size = Math.min(h - 8, (int) (w * 0.58));
        if (size < 70) {
            g2.dispose();
            return;
        }
        float tebal = Math.max(12f, size * 0.14f);
        double ringX = 4;
        double ringY = (h - size) / 2.0;
        double d = size - tebal;
        double ax = ringX + tebal / 2.0;
        double ay = ringY + tebal / 2.0;

        // Cincin latar (dipakai juga saat total = 0)
        g2.setStroke(new BasicStroke(tebal, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        if (total == 0) {
            g2.setColor(new Color(63, 63, 70));
            g2.draw(new Ellipse2D.Double(ax, ay, d, d));
        } else {
            double sapuR = 360.0 * reguler / total;
            double sapuV = 360.0 - sapuR;
            double celah = (reguler > 0 && vip > 0) ? 2.5 : 0;
            if (reguler > 0) {
                g2.setColor(Theme.RED);
                g2.draw(new Arc2D.Double(ax, ay, d, d, 90 - celah / 2, -(sapuR - celah), Arc2D.OPEN));
            }
            if (vip > 0) {
                g2.setColor(Theme.MUTED);
                g2.draw(new Arc2D.Double(ax, ay, d, d, 90 - sapuR - celah / 2, -(sapuV - celah), Arc2D.OPEN));
            }
        }

        // Teks tengah
        double cx = ringX + size / 2.0;
        double cy = ringY + size / 2.0;
        String angka = String.valueOf(total);
        g2.setFont(Theme.font(Font.BOLD, Math.max(24f, size * 0.22f)));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Theme.TEXT);
        g2.drawString(angka, (int) Math.round(cx - fm.stringWidth(angka) / 2.0), (int) Math.round(cy + fm.getAscent() * 0.25));
        g2.setFont(Theme.font(Font.PLAIN, Math.max(11f, size * 0.07f)));
        fm = g2.getFontMetrics();
        g2.setColor(Theme.TEXT_DIM);
        String ket = "Member hadir";
        g2.drawString(ket, (int) Math.round(cx - fm.stringWidth(ket) / 2.0), (int) Math.round(cy + size * 0.17));

        // Legenda vertikal di kanan cincin
        int lx = (int) (ringX + size + Math.max(20, w / 14));
        int tinggiBaris = 66;
        int y0 = (h - (tinggiBaris * 2 + 6)) / 2;
        gambarLegenda(g2, lx, y0, Theme.RED, "Reguler", reguler, total);
        gambarLegenda(g2, lx, y0 + tinggiBaris + 6, Theme.MUTED, "VIP", vip, total);
        g2.dispose();
    }

    private void gambarLegenda(Graphics2D g2, int x, int y, Color warna, String nama, int jumlah, int total) {
        g2.setColor(warna);
        g2.fillRoundRect(x, y + 4, 10, 10, 4, 4);
        g2.setFont(Theme.font(Font.PLAIN, 13));
        g2.setColor(Theme.TEXT_DIM);
        g2.drawString(nama, x + 18, y + 14);

        String angka = String.valueOf(jumlah);
        g2.setFont(Theme.font(Font.BOLD, 26));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Theme.TEXT);
        g2.drawString(angka, x, y + 48);

        String persen = total == 0 ? "0%" : String.format(ID, "%.1f%%", jumlah * 100.0 / total);
        g2.setFont(Theme.font(Font.PLAIN, 12));
        g2.setColor(Theme.TEXT_DIM);
        g2.drawString(persen, x + fm.stringWidth(angka) + 10, y + 48);
    }
}
