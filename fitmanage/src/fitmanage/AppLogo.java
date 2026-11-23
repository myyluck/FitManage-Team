package fitmanage;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

/**
 * Pemuat logo aplikasi.
 *
 * File logo diletakkan di folder package fitmanage (satu folder dengan file .java).
 * Nama yang dicari: logo.png, logo_fitManage.png, dst. Jika tidak ada yang cocok,
 * dicari file apa pun berawalan "logo" (png/jpg/jpeg) di folder itu.
 * Jika tetap tidak ketemu, icon(...) mengembalikan null dan aplikasi tetap jalan.
 */
class AppLogo {

    private static final String[] NAMA_FILE = {
        "logo.png", "logo_fitManage.png", "logo_fitmanage.png", "logo_FitManage.png"
    };

    private static BufferedImage original;
    private static boolean loaded = false;

    /** Icon logo berukuran size x size (px), atau null jika logo tidak ada. */
    static Icon icon(int size) {
        BufferedImage img = load();
        return (img == null) ? null : new LogoIcon(img, size);
    }

    private static synchronized BufferedImage load() {
        if (!loaded) {
            loaded = true;
            try {
                URL url = null;
                for (String nama : NAMA_FILE) {
                    url = AppLogo.class.getResource("/fitmanage/" + nama);
                    if (url != null) break;
                }
                if (url == null) url = cariFileLogo();
                if (url != null) {
                    original = javax.imageio.ImageIO.read(url);
                } else {
                    System.err.println("[Logo] File logo tidak ditemukan di package fitmanage.");
                }
            } catch (Exception ex) {
                System.err.println("[Logo] Gagal memuat logo: " + ex.getMessage());
            }
        }
        return original;
    }

    // Cadangan: cari file berawalan "logo" di folder package (saat dijalankan dari folder kelas)
    private static URL cariFileLogo() {
        try {
            URL dir = AppLogo.class.getResource("/fitmanage/");
            if (dir == null || !"file".equals(dir.getProtocol())) return null;
            File[] files = new File(dir.toURI()).listFiles();
            if (files == null) return null;
            for (File f : files) {
                String n = f.getName().toLowerCase();
                if (n.startsWith("logo") && (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg"))) {
                    return f.toURI().toURL();
                }
            }
        } catch (Exception ignored) {
            // lewati, anggap tidak ada
        }
        return null;
    }

    // Icon yang menggambar ulang logo dengan kualitas halus
    // (tetap tajam di layar dengan skala 125% / 150% / 200%).
    private static class LogoIcon implements Icon {
        private final BufferedImage src;
        private final int size;
        private BufferedImage cache;
        private int cachePx = -1;

        LogoIcon(BufferedImage src, int size) {
            this.src = src;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            double scale = g2.getTransform().getScaleX();
            int px = Math.max(1, (int) Math.round(size * scale));
            if (cache == null || cachePx != px) {
                cache = scaleSmooth(src, px);
                cachePx = px;
            }
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(cache, x, y, size, size, null);
            g2.dispose();
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }

        // Perkecil bertahap (dibagi 2 berulang) supaya hasilnya halus, tidak bergerigi
        private static BufferedImage scaleSmooth(BufferedImage img, int target) {
            int w = img.getWidth();
            while (w / 2 >= target) {
                w /= 2;
                img = resize(img, w);
            }
            return resize(img, target);
        }

        private static BufferedImage resize(BufferedImage img, int px) {
            BufferedImage out = new BufferedImage(px, px, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(img, 0, 0, px, px, null);
            g2.dispose();
            return out;
        }
    }
}
