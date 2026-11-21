package fitmanage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Navbar atas (#161618): logo + "Fit Manage" di kiri, tab navigasi di tengah
 * (tab aktif: teks oranye + garis oranye 3px di bawah), profil Admin di kanan.
 * Pemilihan tab dikirim lewat IntConsumer (indeks tab) -> dipakai untuk CardLayout.show().
 */
public class TopNavBar extends JPanel {

    private static final int TINGGI = 68;

    private final List<NavTab> tabs = new ArrayList<>();
    private final IntConsumer onSelect;
    private final JLabel lblNama = Theme.label("Admin", Font.BOLD, 14, Theme.TEXT);
    private final JLabel lblRole = Theme.label("Admin Utama", Font.PLAIN, 12, Theme.TEXT_DIM);
    private final Avatar avatar = new Avatar("A");
    private int aktif = 0;

    public TopNavBar(String[] menu, IntConsumer onSelect) {
        super(new BorderLayout(28, 0));
        this.onSelect = onSelect;
        setBackground(Theme.NAV_BG);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 24, 1, 24));
        setPreferredSize(new Dimension(0, TINGGI));

        add(buatBrand(), BorderLayout.WEST);
        add(buatTabs(menu), BorderLayout.CENTER);
        add(buatProfil(), BorderLayout.EAST);
        setAktif(0);
    }

    private JPanel buatBrand() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 12);
        p.add(new BrandMark(), c);
        c.insets = new Insets(0, 0, 0, 0);
        p.add(Theme.label("Fit Manage", Font.BOLD, 22, Color.WHITE), c);
        return p;
    }

    private JPanel buatTabs(String[] menu) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        for (int i = 0; i < menu.length; i++) {
            NavTab t = new NavTab(menu[i], i);
            tabs.add(t);
            p.add(t);
        }
        return p;
    }

    private JPanel buatProfil() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        lblNama.setHorizontalAlignment(SwingConstants.RIGHT);
        lblRole.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        lblNama.setAlignmentX(RIGHT_ALIGNMENT);
        lblRole.setAlignmentX(RIGHT_ALIGNMENT);
        teks.add(lblNama);
        teks.add(lblRole);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 12);
        p.add(teks, c);
        c.insets = new Insets(0, 0, 0, 0);
        p.add(avatar, c);
        return p;
    }

    /** Ganti nama & role yang tampil di kanan atas. Avatar memakai huruf pertama nama. */
    public void setUser(String nama, String role) {
        lblNama.setText(nama);
        lblRole.setText(role);
        avatar.setHuruf(nama == null || nama.isEmpty() ? "A" : nama.substring(0, 1).toUpperCase());
    }

    /** Menandai tab aktif TANPA memicu callback. */
    public void setAktif(int index) {
        aktif = index;
        for (NavTab t : tabs) {
            t.repaint();
        }
    }

    /** Menandai tab aktif DAN memicu callback (dipakai saat tab diklik). */
    public void pilih(int index) {
        setAktif(index);
        onSelect.accept(index);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Theme.BORDER);
        g.fillRect(0, getHeight() - 1, getWidth(), 1);
    }

    // ---------------------------------------------------------------
    private class NavTab extends JComponent {
        private final String teks;
        private final int index;
        private boolean hover;

        NavTab(String teks, int index) {
            this.teks = teks;
            this.index = index;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    pilih(NavTab.this.index);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(Font.PLAIN, 14));
            return new Dimension(fm.stringWidth(teks) + 32, TINGGI);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(getPreferredSize().width, Short.MAX_VALUE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean on = index == aktif;
            g2.setFont(Theme.font(Font.PLAIN, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(on ? Theme.ORANGE : (hover ? Color.WHITE : new Color(212, 212, 216)));
            int x = (getWidth() - fm.stringWidth(teks)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(teks, x, y);
            if (on) {
                g2.setColor(Theme.ORANGE);
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
            }
            g2.dispose();
        }
    }

    private static class BrandMark extends JComponent {
        BrandMark() {
            setPreferredSize(new Dimension(38, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UiIcons.paint(g2, UiIcons.Type.BRAND, 0, 0, 38, Theme.ORANGE);
            g2.dispose();
        }
    }

    private static class Avatar extends JComponent {
        private String huruf;

        Avatar(String huruf) {
            this.huruf = huruf;
            setPreferredSize(new Dimension(42, 42));
        }

        void setHuruf(String h) {
            this.huruf = h;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, Theme.RED, getWidth(), getHeight(), Theme.ORANGE));
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(Font.BOLD, 17));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(huruf, (getWidth() - fm.stringWidth(huruf)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
