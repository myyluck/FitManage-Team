package fitmanage;

import java.awt.*;
import java.util.Locale;
import javax.swing.JPanel;

public class PieChartPanel extends JPanel {
    public PieChartPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        DataStore store = DataStore.getInstance();
        int reguler = store.getCheckInRegulerHariIni();
        int vip = store.getCheckInVipHariIni();
        int total = Math.max(1, reguler + vip);

        double pctReg = (reguler * 100.0) / total;
        double pctVip = (vip * 100.0) / total;
        int angleReg = (int) Math.round((reguler / (double) total) * 360.0);
        int angleVip = 360 - angleReg;

        int size = Math.max(110, Math.min((int) (getWidth() * 0.48), getHeight() - 18));
        int legendAreaWidth = 108;
        int gap = 24;
        int totalContentWidth = size + gap + legendAreaWidth;

        int x = Math.max(10, (getWidth() - totalContentWidth) / 2);
        int y = (getHeight() - size) / 2;
        int thickness = Math.max(16, size / 6);

        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        int pad = thickness / 2;

        // Segmen Abu-abu (VIP)
        g2.setColor(ThemeUI.DONUT_GRAY);
        g2.drawArc(x + pad, y + pad, size - thickness, size - thickness, 90, angleVip);

        // Segmen Merah (Reguler)
        g2.setColor(ThemeUI.DONUT_RED);
        g2.drawArc(x + pad, y + pad, size - thickness, size - thickness, 90, -angleReg);

        // Teks Tengah Donut
        g2.setColor(ThemeUI.TEXT_WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        FontMetrics fmVal = g2.getFontMetrics();
        String totalStr = String.valueOf(reguler + vip);
        g2.drawString(totalStr, x + (size - fmVal.stringWidth(totalStr)) / 2, y + size / 2 + 2);

        g2.setColor(ThemeUI.TEXT_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fmSub = g2.getFontMetrics();
        String subStr = "Member hadir";
        g2.drawString(subStr, x + (size - fmSub.stringWidth(subStr)) / 2, y + size / 2 + 19);

        // Legend Kanan
        int legX = x + size + gap;
        int legY1 = y + size / 2 - 28;
        Locale idLocale = new Locale("id", "ID");

        g2.setColor(ThemeUI.DONUT_RED);
        g2.fillOval(legX, legY1, 8, 8);
        g2.setColor(ThemeUI.TEXT_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("Reguler", legX + 14, legY1 + 8);

        g2.setColor(ThemeUI.TEXT_WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        String regStr = String.valueOf(reguler);
        g2.drawString(regStr, legX, legY1 + 29);
        g2.setColor(ThemeUI.TEXT_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString(String.format(idLocale, "%.1f%%", pctReg), legX + g2.getFontMetrics(new Font("SansSerif", Font.BOLD, 16)).stringWidth(regStr) + 8, legY1 + 29);

        int legY2 = legY1 + 56;
        g2.setColor(ThemeUI.DONUT_GRAY);
        g2.fillOval(legX, legY2, 8, 8);
        g2.setColor(ThemeUI.TEXT_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("VIP", legX + 14, legY2 + 8);

        g2.setColor(ThemeUI.TEXT_WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        String vipStr = String.valueOf(vip);
        g2.drawString(vipStr, legX, legY2 + 29);
        g2.setColor(ThemeUI.TEXT_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString(String.format(idLocale, "%.1f%%", pctVip), legX + g2.getFontMetrics(new Font("SansSerif", Font.BOLD, 16)).stringWidth(vipStr) + 8, legY2 + 29);

        g2.dispose();
    }
}