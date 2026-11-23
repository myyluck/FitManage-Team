package fitmanage;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import javax.swing.JPanel;

public class LineChartPanel extends JPanel {
    private final String[] months = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};

    public LineChartPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padLeft = 36, padRight = 16, padTop = 28, padBottom = 26;
        int w = getWidth() - padLeft - padRight;
        int h = getHeight() - padTop - padBottom;
        if (w <= 0 || h <= 0) {
            g2.dispose();
            return;
        }

        int[] orangeData = DataStore.getInstance().getKunjunganBulanan();
        int[] blueData = DataStore.getInstance().getPendaftaranBulanan();

        // Hitung garis tren dinamis (Total Aktivitas = Kunjungan + Pendaftaran)
        int[] trendLine = new int[months.length];
        int highest = 0;
        for (int i = 0; i < months.length; i++) {
            int kunj = (orangeData != null && i < orangeData.length) ? orangeData[i] : 0;
            int daftar = (blueData != null && i < blueData.length) ? blueData[i] : 0;
            trendLine[i] = kunj + daftar;
            if (trendLine[i] > highest) {
                highest = trendLine[i];
            }
        }

        // Skala sumbu Y otomatis (dibagi 5 garis grid agar kelipatan angka selalu bulat)
        int step = Math.max(2, (int) Math.ceil(highest / 5.0));
        if (step % 2 != 0 && step > 5) {
            step++;
        }
        int maxVal = step * 5;

        // Gambar legenda ringkas di pojok kanan atas
        drawLegend(g2, padLeft + w);

        // Gambar garis grid & label angka sumbu Y
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        for (int val = 0; val <= maxVal; val += step) {
            int y = padTop + h - (int) ((val / (double) maxVal) * h);
            g2.setColor(new Color(45, 45, 45));
            g2.drawLine(padLeft, y, padLeft + w, y);

            g2.setColor(ThemeUI.TEXT_GRAY);
            String label = String.valueOf(val);
            g2.drawString(label, padLeft - fm.stringWidth(label) - 10, y + 4);
        }

        int slotWidth = w / months.length;
        int barWidth = Math.max(8, Math.min(22, slotWidth / 3));
        Point2D.Double[] linePoints = new Point2D.Double[months.length];

        for (int i = 0; i < months.length; i++) {
            int centerX = padLeft + i * slotWidth + slotWidth / 2;
            int kunj = (orangeData != null && i < orangeData.length) ? orangeData[i] : 0;
            int daftar = (blueData != null && i < blueData.length) ? blueData[i] : 0;

            // Batang Oranye: Kunjungan (Check-In)
            int h1 = (int) ((kunj / (double) maxVal) * h);
            g2.setColor(ThemeUI.ACCENT_ORANGE);
            g2.fillRoundRect(centerX - barWidth - 2, padTop + h - h1, barWidth, h1, 4, 4);

            // Batang Biru: Pendaftaran Member
            int h2 = (int) ((daftar / (double) maxVal) * h);
            g2.setColor(ThemeUI.BAR_SLATE);
            g2.fillRoundRect(centerX + 2, padTop + h - h2, barWidth, h2, 4, 4);

            // Label Bulan
            g2.setColor(ThemeUI.TEXT_GRAY);
            int lblW = fm.stringWidth(months[i]);
            g2.drawString(months[i], centerX - lblW / 2, padTop + h + 17);

            // Titik koordinat garis tren
            double ty = padTop + h - ((trendLine[i] / (double) maxVal) * h);
            linePoints[i] = new Point2D.Double(centerX, ty);
        }

        // Gambar kurva halus garis tren
        g2.setColor(new Color(210, 210, 210));
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D path = new Path2D.Double();
        path.moveTo(linePoints[0].x, linePoints[0].y);
        for (int i = 0; i < linePoints.length - 1; i++) {
            double midX = (linePoints[i].x + linePoints[i + 1].x) / 2.0;
            path.curveTo(midX, linePoints[i].y, midX, linePoints[i + 1].y, linePoints[i + 1].x, linePoints[i + 1].y);
        }
        g2.draw(path);

        // Titik penanda kecil pada bulan yang memiliki aktivitas
        for (int i = 0; i < linePoints.length; i++) {
            if (trendLine[i] > 0) {
                g2.setColor(ThemeUI.BG_CARD);
                g2.fillOval((int) linePoints[i].x - 4, (int) linePoints[i].y - 4, 8, 8);
                g2.setColor(new Color(235, 235, 235));
                g2.fillOval((int) linePoints[i].x - 2, (int) linePoints[i].y - 2, 5, 5);
            }
        }

        g2.dispose();
    }

    private void drawLegend(Graphics2D g2, int rightX) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();

        String[] labels = {"Kunjungan", "Pendaftaran", "Total Tren"};
        Color[] colors = {ThemeUI.ACCENT_ORANGE, ThemeUI.BAR_SLATE, new Color(210, 210, 210)};

        int x = rightX;
        for (int i = labels.length - 1; i >= 0; i--) {
            int textW = fm.stringWidth(labels[i]);
            x -= textW;
            g2.setColor(ThemeUI.TEXT_GRAY);
            g2.drawString(labels[i], x, 12);

            x -= 14;
            g2.setColor(colors[i]);
            if (i == 2) {
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(x, 8, x + 9, 8);
            } else {
                g2.fillRoundRect(x, 4, 9, 9, 3, 3);
            }
            x -= 14;
        }
    }
}