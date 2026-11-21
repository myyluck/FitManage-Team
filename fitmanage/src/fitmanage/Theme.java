package fitmanage;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Pusat tema "Dark Charcoal & Vibrant Orange" untuk seluruh aplikasi Fit Manage.
 * Semua warna, font, dan style komponen diatur di sini supaya seragam.
 *
 * Cara pakai di main():  Theme.install();  (menggantikan FlatLightLaf yang lama)
 */
public final class Theme {

    private Theme() { }

    // ---------------- Palet warna ----------------
    public static final Color BG        = new Color(18, 18, 20);    // #121214 background window
    public static final Color NAV_BG    = new Color(22, 22, 24);    // #161618 top navbar
    public static final Color CARD      = new Color(28, 28, 31);    // #1C1C1F kartu & panel
    public static final Color BORDER    = new Color(44, 44, 48);    // #2C2C30 border halus
    public static final Color INPUT_BG  = new Color(24, 24, 27);    // background kolom input

    public static final Color ORANGE    = new Color(249, 115, 22);  // #F97316 aksen utama
    public static final Color RED       = new Color(239, 68, 68);   // #EF4444 aksen kedua
    public static final Color MUTED     = new Color(82, 82, 91);    // #52525B abu-abu
    public static final Color SLATE     = new Color(100, 116, 139); // batang "Pendaftaran"

    public static final Color TEXT      = new Color(244, 244, 245);
    public static final Color TEXT_DIM  = new Color(161, 161, 170);

    public static final Color GREEN_TEXT = new Color(74, 222, 128); // #4ADE80
    public static final Color GREEN_BG   = new Color(20, 83, 45);   // #14532D
    public static final Color RED_TEXT   = new Color(248, 113, 113);
    public static final Color RED_BG     = new Color(69, 10, 10);
    public static final Color AMBER_TEXT = new Color(251, 191, 36);
    public static final Color AMBER_BG   = new Color(66, 40, 6);
    public static final Color ROW_SELECT = new Color(58, 36, 22);

    /** Radius sudut kartu (px). */
    public static final int ARC = 16;

    private static final String FONT_FAMILY = pilihFont();

    private static String pilihFont() {
        Set<String> ada = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : new String[]{"Segoe UI", "Inter", "Helvetica Neue", "Ubuntu"}) {
            if (ada.contains(f)) {
                return f;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, Math.round(size)).deriveFont(style, size);
    }

    // ---------------- Instalasi Look & Feel ----------------
    /** Pasang FlatLaf Dark + warna aksen oranye. Panggil SEBELUM membuat JFrame apa pun. */
    public static void install() {
        try {
            System.setProperty("flatlaf.useWindowDecorations", "true");
            FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#F97316"));
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("FlatLaf Dark gagal diinisialisasi: " + ex.getMessage());
        }

        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 14);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("Component.focusedBorderColor", ORANGE);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);

        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("Dialog.background", CARD);
        UIManager.put("TextField.background", INPUT_BG);
        UIManager.put("PasswordField.background", INPUT_BG);
        UIManager.put("ComboBox.background", INPUT_BG);
        UIManager.put("Spinner.background", INPUT_BG);
        UIManager.put("List.background", INPUT_BG);
    }

    // ---------------- Helper komponen ----------------
    public static JLabel label(String teks, int style, float size, Color warna) {
        JLabel l = new JLabel(teks);
        l.setFont(font(style, size));
        l.setForeground(warna);
        return l;
    }

    private static void gayaTombol(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(font(Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setMargin(new Insets(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Tombol oranye solid (aksi utama, mis. Check-In). */
    public static void tombolUtama(JButton b) {
        gayaTombol(b, ORANGE, Color.WHITE);
    }

    /** Tombol gelap (aksi sekunder, mis. + Tambah Member). */
    public static void tombolGelap(JButton b) {
        gayaTombol(b, new Color(30, 41, 59), TEXT);
    }

    /** Tombol netral abu-abu gelap (Cari, Batal, dll). */
    public static void tombolNetral(JButton b) {
        gayaTombol(b, new Color(39, 39, 42), TEXT);
    }

    /** Tombol merah (Hapus). */
    public static void tombolBahaya(JButton b) {
        gayaTombol(b, RED, Color.WHITE);
    }

    public static void gayaInput(JTextField f, String placeholder) {
        f.setFont(font(Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 40));
        f.setBackground(INPUT_BG);
        f.setForeground(TEXT);
        f.setCaretColor(ORANGE);
        if (placeholder != null) {
            f.putClientProperty("JTextField.placeholderText", placeholder);
        }
        f.putClientProperty("JComponent.roundRect", true);
    }

    public static void gayaCombo(JComboBox<?> c) {
        c.setFont(font(Font.PLAIN, 14));
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 40));
        c.putClientProperty("JComponent.roundRect", true);
    }

    // ---------------- Tabel gelap ----------------
    /**
     * Mengatur gaya tabel gelap dan mengembalikan JScrollPane-nya (siap ditaruh di kartu).
     * Sel angka dibuat rata kiri supaya sama dengan kolom lain.
     */
    public static JScrollPane gayaTabel(JTable t) {
        t.setBackground(CARD);
        t.setForeground(TEXT);
        t.setFont(font(Font.PLAIN, 14));
        t.setRowHeight(46);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(BORDER);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(ROW_SELECT);
        t.setSelectionForeground(Color.WHITE);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.setFocusable(false);

        CellRenderer isi = new CellRenderer();
        t.setDefaultRenderer(Object.class, isi);
        t.setDefaultRenderer(Number.class, isi);

        t.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setPreferredSize(new Dimension(100, 44));

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD);
        sp.setBackground(CARD);
        return sp;
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                       boolean foc, int row, int col) {
            super.getTableCellRendererComponent(table, value, false, false, row, col);
            setOpaque(true);
            setBackground(NAV_BG);
            setForeground(TEXT);
            setFont(font(Font.BOLD, 13));
            setHorizontalAlignment(LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                       boolean foc, int row, int col) {
            super.getTableCellRendererComponent(table, value, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setHorizontalAlignment(LEFT);
            return this;
        }
    }
}
