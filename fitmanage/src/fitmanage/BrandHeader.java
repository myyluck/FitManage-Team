package fitmanage;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Logo + tulisan "FIT MANAGE" bergaya mewah (huruf kapital serif, jarak antar huruf
 * lebar, warna putih). Dipakai di sidebar Dashboard dan halaman Login.
 *
 * - SAMPING : logo di kiri, tulisan di kanan (sidebar)
 * - ATAS    : logo di atas, tulisan di bawahnya (halaman login)
 * Seluruh susunan otomatis berada di tengah secara horizontal, dan ukuran
 * tulisan mengecil sendiri jika ruangnya sempit.
 *
 * Font: jika ada file brand_font.ttf (atau .otf) di folder package fitmanage,
 * file itulah yang dipakai (bawaan: Lora, lisensi SIL Open Font License). Jika tidak ada, dipilih font serif elegan yang
 * terpasang di komputer (Constantia, Palatino Linotype, Georgia, dst).
 */
public class BrandHeader extends JComponent {

    public enum Susunan { SAMPING, ATAS }

    private static final String TEKS = "FIT MANAGE";

    // ===== Pengaturan tampilan (silakan diubah) =====
    private static final Color WARNA_TULISAN = Color.WHITE;
    private static final int   JARAK_LOGO_TEKS = 12;
    private static final String[] FONT_FILE  = {"/fitmanage/brand_font.ttf", "/fitmanage/brand_font.otf"};
    private static final String[] FONT_PILIHAN = {
        "Cinzel", "Trajan Pro", "Playfair Display", "Bodoni MT", "Constantia",
        "Palatino Linotype", "Georgia"
    };

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static Font fontDasar;

    private final Susunan susunan;
    private final int ukuranLogo;
    private final float ukuranFontMax;
    private final float jarakHuruf;
    private final Icon logo;

    public BrandHeader(Susunan susunan, int ukuranLogo, float ukuranFontMax, float jarakHuruf) {
        this.susunan = susunan;
        this.ukuranLogo = ukuranLogo;
        this.ukuranFontMax = ukuranFontMax;
        this.jarakHuruf = jarakHuruf;
        this.logo = AppLogo.icon(ukuranLogo);
        setOpaque(false);
    }

    // ---------------------------------------------------------------
    // Font
    // ---------------------------------------------------------------
    private static synchronized Font fontDasar() {
        if (fontDasar != null) return fontDasar;

        // 1) File font yang ditaruh di package
        for (String path : FONT_FILE) {
            try (InputStream in = BrandHeader.class.getResourceAsStream(path)) {
                if (in != null) {
                    Font f = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(Font.BOLD);
                    if (f.canDisplay('F') && f.canDisplay('M')) {   // pastikan hurufnya benar-benar terbaca
                        fontDasar = f;
                        return fontDasar;
                    }
                    System.err.println("[Font] " + path + " tidak bisa dibaca Java, memakai font sistem.");
                }
            } catch (Exception ex) {
                System.err.println("[Font] Gagal memuat " + path + ": " + ex.getMessage());
            }
        }

        // 2) Font sistem yang tersedia
        Set<String> ada = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String nama : FONT_PILIHAN) {
            if (ada.contains(nama)) {
                fontDasar = new Font(nama, Font.BOLD, 1);
                return fontDasar;
            }
        }
        fontDasar = new Font(Font.SERIF, Font.BOLD, 1);
        return fontDasar;
    }

    private static double lebarTeks(Font f, String s, double jarak) {
        double w = 0;
        for (int i = 0; i < s.length(); i++) {
            w += f.getStringBounds(s.substring(i, i + 1), FRC).getWidth();
        }
        return w + jarak * (s.length() - 1);
    }

    // ---------------------------------------------------------------
    // Ukuran
    // ---------------------------------------------------------------
    private int tinggiTeks(Font f) {
        return (int) Math.ceil(f.getLineMetrics(TEKS, FRC).getHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        Font f = fontDasar().deriveFont(ukuranFontMax);
        double tw = lebarTeks(f, TEKS, jarakHuruf);
        int th = tinggiTeks(f);
        int lg = (logo != null) ? ukuranLogo : 0;
        if (susunan == Susunan.SAMPING) {
            int w = (int) Math.ceil(lg + (lg > 0 ? JARAK_LOGO_TEKS : 0) + tw);
            return new Dimension(w, Math.max(lg, th));
        }
        int w = (int) Math.ceil(Math.max(lg, tw));
        return new Dimension(w, lg + (lg > 0 ? JARAK_LOGO_TEKS : 0) + th);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, getPreferredSize().height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, getPreferredSize().height);
    }

    // ---------------------------------------------------------------
    // Gambar
    // ---------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        int lg = (logo != null) ? ukuranLogo : 0;
        int gap = (lg > 0) ? JARAK_LOGO_TEKS : 0;
        int w = getWidth();
        int h = getHeight();

        // Kecilkan font sampai seluruh susunan muat di lebar yang tersedia
        float size = ukuranFontMax;
        Font font = fontDasar().deriveFont(size);
        double tw = lebarTeks(font, TEKS, jarakHuruf);
        double lebarGrup = (susunan == Susunan.SAMPING) ? lg + gap + tw : Math.max(lg, tw);
        while (size > 12f && lebarGrup > w) {
            size -= 1f;
            font = fontDasar().deriveFont(size);
            tw = lebarTeks(font, TEKS, jarakHuruf);
            lebarGrup = (susunan == Susunan.SAMPING) ? lg + gap + tw : Math.max(lg, tw);
        }
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int th = tinggiTeks(font);

        double xTeks;
        int yBaris;
        if (susunan == Susunan.SAMPING) {
            double x0 = (w - lebarGrup) / 2.0;
            if (logo != null) logo.paintIcon(this, g2, (int) Math.round(x0), (h - lg) / 2);
            xTeks = x0 + lg + gap;
            yBaris = (h - th) / 2 + fm.getAscent();
        } else {
            int tinggiGrup = lg + gap + th;
            int y0 = (h - tinggiGrup) / 2;
            if (logo != null) logo.paintIcon(this, g2, (w - lg) / 2, y0);
            xTeks = (w - tw) / 2.0;
            yBaris = y0 + lg + gap + fm.getAscent();
        }

        // Tulisan putih, digambar huruf per huruf agar jarak antar huruf lebar
        g2.setColor(WARNA_TULISAN);
        double x = xTeks;
        for (int i = 0; i < TEKS.length(); i++) {
            String ch = TEKS.substring(i, i + 1);
            g2.drawString(ch, (float) x, yBaris);
            x += font.getStringBounds(ch, FRC).getWidth() + jarakHuruf;
        }
        g2.dispose();
    }
}
