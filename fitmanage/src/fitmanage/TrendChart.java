package fitmanage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

/**
 * Chart batang ganda (Kunjungan = oranye, Pendaftaran = abu slate) Jan-Des
 * + kurva tren halus + garis grid horizontal. Digambar murni dengan Graphics2D.
 * Skala sumbu Y menyesuaikan data (mis. 0-60), dan ada tooltip saat kursor diarahkan.
 */
public class TrendChart extends JComponent {

    public static final String[] BULAN = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};

    private static final int PAD_LEFT = 44;
    private static final int PAD_RIGHT = 10;
    private static final int PAD_TOP = 30;
    private static final int PAD_BOTTOM = 30;
    private static final Color GRID = new Color(39, 39, 43);

    private int[] kunjungan = new int[12];
    private int[] pendaftaran = new int[12];
    private int hover = -1;

    public TrendChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 260));
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int pw = getWidth() - PAD_LEFT - PAD_RIGHT;
                int idx = -1;
                if (pw > 0 && e.getX() >= PAD_LEFT && e.getX() <= PAD_LEFT + pw) {
                    idx = (int) ((e.getX() - PAD_LEFT) / (pw / 12.0));
                    if (idx > 11) {
                        idx = 11;
                    }
                }
                if (idx != hover) {
                    hover = idx;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hover != -1) {
                    hover = -1;
                    repaint();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    /** Isi data 12 bulan (indeks 0 = Jan). Array yang lebih pendek dilengkapi nol. */
    public void setData(int[] kunjungan, int[] pendaftaran) {
        this.kunjungan = salin(kunjungan);
        this.pendaftaran = salin(pendaftaran);
        repaint();
    }

    private static int[] salin(int[] src) {
        int[] out = new int[12];
        if (src != null) {
            System.arraycopy(src, 0, out, 0, Math.min(12, src.length));
        }
        return out;
    }

    private static int langkahSkala(int max) {
        int[] kandidat = {1, 2, 5, 10, 20, 25, 50, 100, 200, 250, 500, 1000, 2000, 5000, 10000};
        for (int s : kandidat) {
            if ((max + s - 1) / s <= 6) {
                return s;
            }
        }
        return 20000;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth();
        int h = getHeight();
        int pw = w - PAD_LEFT - PAD_RIGHT;
        int ph = h - PAD_TOP - PAD_BOTTOM;
        if (pw < 120 || ph < 60) {
            g2.dispose();
            return;
        }

        int max = 0;
        for (int i = 0; i < 12; i++) {
            max = Math.max(max, Math.max(kunjungan[i], pendaftaran[i]));
        }
        int step;
        int axisMax;
        if (max == 0) {
            step = 10;
            axisMax = 60;
        } else {
            step = langkahSkala(max);
            axisMax = ((max + step - 1) / step) * step;
        }
        int jumlahGaris = axisMax / step;
        int baseY = PAD_TOP + ph;

        // ---- Grid & label sumbu Y ----
        g2.setFont(Theme.font(Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= jumlahGaris; i++) {
            int nilai = i * step;
            int y = baseY - (int) Math.round(ph * (double) nilai / axisMax);
            g2.setColor(i == 0 ? new Color(63, 63, 70) : GRID);
            g2.drawLine(PAD_LEFT, y, PAD_LEFT + pw, y);
            String s = String.valueOf(nilai);
            g2.setColor(Theme.TEXT_DIM);
            g2.drawString(s, PAD_LEFT - 10 - fm.stringWidth(s), y + fm.getAscent() / 2 - 1);
        }

        // ---- Batang ganda ----
        double gw = pw / 12.0;
        double bw = Math.min(gw * 0.30, 26);
        double gap = 3;
        double[] px = new double[12];
        double[] py = new double[12];
        for (int i = 0; i < 12; i++) {
            double cx = PAD_LEFT + gw * (i + 0.5);
            if (i == hover) {
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect((int) (PAD_LEFT + gw * i) + 2, PAD_TOP, (int) gw - 4, ph, 10, 10);
            }
            double hk = ph * kunjungan[i] / (double) axisMax;
            double hp = ph * pendaftaran[i] / (double) axisMax;
            gambarBatang(g2, cx - gap / 2 - bw, baseY, bw, hk, Theme.ORANGE);
            gambarBatang(g2, cx + gap / 2, baseY, bw, hp, Theme.SLATE);

            px[i] = cx;
            double rata = (kunjungan[i] + pendaftaran[i]) / 2.0;
            py[i] = baseY - ph * rata / axisMax;
        }

        // ---- Kurva tren halus (Catmull-Rom -> Bezier) ----
        Path2D.Double path = new Path2D.Double();
        path.moveTo(px[0], py[0]);
        for (int i = 0; i < 11; i++) {
            int i0 = Math.max(i - 1, 0);
            int i3 = Math.min(i + 2, 11);
            double c1x = px[i] + (px[i + 1] - px[i0]) / 6.0;
            double c1y = klem(py[i] + (py[i + 1] - py[i0]) / 6.0, baseY);
            double c2x = px[i + 1] - (px[i3] - px[i]) / 6.0;
            double c2y = klem(py[i + 1] - (py[i3] - py[i]) / 6.0, baseY);
            path.curveTo(c1x, c1y, c2x, c2y, px[i + 1], py[i + 1]);
        }
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(161, 161, 170, 210));
        g2.draw(path);

        // ---- Label bulan ----
        g2.setFont(Theme.font(Font.PLAIN, 13));
        fm = g2.getFontMetrics();
        g2.setColor(Theme.TEXT_DIM);
        for (int i = 0; i < 12; i++) {
            g2.drawString(BULAN[i], (int) Math.round(px[i] - fm.stringWidth(BULAN[i]) / 2.0), baseY + 20);
        }

        // ---- Legenda kanan atas ----
        g2.setFont(Theme.font(Font.PLAIN, 12));
        fm = g2.getFontMetrics();
        String[] nama = {"Kunjungan", "Pendaftaran"};
        Color[] warna = {Theme.ORANGE, Theme.SLATE};
        int total = 0;
        for (String n : nama) {
            total += 9 + 6 + fm.stringWidth(n) + 16;
        }
        int lx = w - PAD_RIGHT - total + 16;
        for (int i = 0; i < nama.length; i++) {
            g2.setColor(warna[i]);
            g2.fillRoundRect(lx, 8, 9, 9, 4, 4);
            g2.setColor(Theme.TEXT_DIM);
            g2.drawString(nama[i], lx + 15, 17);
            lx += 9 + 6 + fm.stringWidth(nama[i]) + 16;
        }

        // ---- Tooltip ----
        if (hover >= 0) {
            gambarTooltip(g2, hover, PAD_LEFT + gw * hover, gw, w);
        }
        g2.dispose();
    }

    private double klem(double y, int baseY) {
        return Math.min(baseY, Math.max(PAD_TOP, y));
    }

    private void gambarBatang(Graphics2D g2, double x, double baseY, double w, double h, Color c) {
        if (h <= 0) {
            return;
        }
        if (h < 1) {
            h = 1;
        }
        g2.setColor(c);
        double r = Math.min(6, Math.min(w / 2, h));
        g2.fill(new RoundRectangle2D.Double(x, baseY - h, w, h, r * 2, r * 2));
        g2.fill(new Rectangle2D.Double(x, baseY - r, w, r)); // sudut bawah dibuat lurus
    }

    private void gambarTooltip(Graphics2D g2, int idx, double groupX, double gw, int totalW) {
        String[] baris = {
            BULAN[idx],
            "Kunjungan: " + kunjungan[idx],
            "Pendaftaran: " + pendaftaran[idx]
        };
        Font fBold = Theme.font(Font.BOLD, 12);
        Font fReg = Theme.font(Font.PLAIN, 12);
        g2.setFont(fBold);
        int lebar = g2.getFontMetrics().stringWidth(baris[0]);
        g2.setFont(fReg);
        for (int i = 1; i < baris.length; i++) {
            lebar = Math.max(lebar, g2.getFontMetrics().stringWidth(baris[i]));
        }
        int bw = lebar + 24;
        int bh = 66;
        int bx = (int) (groupX + gw + 6);
        if (bx + bw > totalW - 4) {
            bx = (int) (groupX - bw - 6);
        }
        int by = PAD_TOP + 6;

        g2.setColor(new Color(39, 39, 42));
        g2.fillRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(bx, by, bw, bh, 12, 12);

        g2.setFont(fBold);
        g2.setColor(Theme.TEXT);
        g2.drawString(baris[0], bx + 12, by + 20);
        g2.setFont(fReg);
        g2.setColor(Theme.ORANGE);
        g2.drawString(baris[1], bx + 12, by + 38);
        g2.setColor(Theme.TEXT_DIM);
        g2.drawString(baris[2], bx + 12, by + 55);
    }
}
