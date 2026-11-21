package fitmanage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer sel tabel yang menggambar teks status sebagai badge "pill" membulat.
 * "Aktif" / "Lunas" / "Tersedia" -> hijau, "Hampir Habis" / "Pending" / "Cuti" -> kuning,
 * "Kedaluwarsa" / "Nonaktif" / "Penuh" -> merah, "Sedang latihan" -> oranye.
 * Pasang dengan: tabel.getColumnModel().getColumn(5).setCellRenderer(new StatusPillRenderer());
 */
public class StatusPillRenderer implements TableCellRenderer {

    private final PillCell sel = new PillCell();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        sel.set(value == null ? "" : value.toString(),
                isSelected ? table.getSelectionBackground() : table.getBackground());
        return sel;
    }

    /** Pasangan {warna latar, warna teks} untuk sebuah status. */
    static Color[] warnaStatus(String status) {
        String s = status == null ? "" : status.trim().toLowerCase();
        switch (s) {
            case "aktif":
            case "lunas":
            case "tersedia":
                return new Color[]{Theme.GREEN_BG, Theme.GREEN_TEXT};
            case "hampir habis":
            case "pending":
            case "cuti":
                return new Color[]{Theme.AMBER_BG, Theme.AMBER_TEXT};
            case "kedaluwarsa":
            case "nonaktif":
            case "penuh":
                return new Color[]{Theme.RED_BG, Theme.RED_TEXT};
            case "sedang latihan":
                return new Color[]{new Color(67, 32, 10), Theme.ORANGE};
            default:
                return new Color[]{new Color(39, 39, 42), Theme.TEXT_DIM};
        }
    }

    private static class PillCell extends JComponent {
        private String teks = "";
        private Color latarBaris = Theme.CARD;

        void set(String teks, Color latarBaris) {
            this.teks = teks;
            this.latarBaris = latarBaris;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(110, 46);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(latarBaris);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (teks != null && !teks.isEmpty()) {
                Color[] warna = warnaStatus(teks);
                g2.setFont(Theme.font(Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int pw = fm.stringWidth(teks) + 24;
                int ph = 26;
                int x = 12;
                int y = (getHeight() - ph) / 2;
                g2.setColor(warna[0]);
                g2.fillRoundRect(x, y, pw, ph, ph, ph);
                g2.setColor(warna[1]);
                g2.drawString(teks, x + 12, y + (ph - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }
}
