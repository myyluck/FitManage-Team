package fitmanage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Kerangka dasar untuk semua form pop-up (Tambah Jadwal, Tambah Trainer, Buat Paket,
 * Export Laporan). Menyediakan judul, area isi, pesan kesalahan, tombol Batal & Simpan,
 * serta helper untuk membuat kolom isian dengan gaya yang seragam.
 *
 * Form berupa JPanel biasa; FormDialog yang membungkusnya menjadi kotak pop-up.
 * Subclass mengisi kolom lewat tambahField(...), lalu menulis isi simpan().
 */
abstract class FormPanel extends JPanel {

    static final Color NAVY    = new Color(10, 25, 60);
    static final Color BLUE    = new Color(0, 131, 176);
    static final Color RED     = new Color(220, 53, 69);
    static final Color GREEN   = new Color(25, 135, 84);
    static final Color GRAY    = new Color(100, 100, 100);
    static final Color CARD_BG = new Color(245, 247, 250);

    /** Area kolom isian (GridBagLayout, 2 kolom). */
    protected final JPanel isi = new JPanel(new GridBagLayout());
    protected final JLabel lblError = new JLabel(" ");

    private final String judul;
    private final JButton btnUtama;
    private Runnable penutup = () -> { };
    private Runnable perbaruiUkuran = () -> { };

    protected FormPanel(String judul, String subJudul, String teksTombol, Color warnaTombol) {
        super(new BorderLayout(0, 14));
        this.judul = judul;
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(22, 30, 22, 30));

        // --- Header ---
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblJudul.setForeground(NAVY);
        lblJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblSub = new JLabel(subJudul);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(GRAY);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(lblJudul);
        header.add(Box.createVerticalStrut(4));
        header.add(lblSub);

        // --- Isi ---
        isi.setOpaque(false);

        // --- Pesan error + tombol ---
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(RED);

        JButton btnBatal = new JButton("Batal");
        btnBatal.setBackground(new Color(240, 240, 240));
        btnBatal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBatal.setPreferredSize(new Dimension(110, 44));
        btnBatal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBatal.addActionListener(e -> tutup());

        btnUtama = new JButton(teksTombol);
        btnUtama.setBackground(warnaTombol);
        btnUtama.setForeground(Color.WHITE);
        btnUtama.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUtama.setPreferredSize(new Dimension(170, 44));
        btnUtama.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUtama.addActionListener(e -> {
            lblError.setText(" ");
            simpan();
        });

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        tombol.setOpaque(false);
        tombol.add(btnBatal);
        tombol.add(btnUtama);

        JPanel bawah = new JPanel(new BorderLayout(0, 8));
        bawah.setOpaque(false);
        bawah.add(lblError, BorderLayout.NORTH);
        bawah.add(tombol, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(isi, BorderLayout.CENTER);
        add(bawah, BorderLayout.SOUTH);
    }

    // =========================================================================
    // Dipakai FormDialog
    // =========================================================================
    String getJudul()               { return judul; }
    JButton getTombolUtama()        { return btnUtama; }
    int getLebarMin()               { return 540; }
    void setPenutup(Runnable r)     { this.penutup = r; }
    void setPerbaruiUkuran(Runnable r) { this.perbaruiUkuran = r; }

    /** Dipanggil setelah form tampil; subclass boleh meng-override untuk memberi fokus awal. */
    void fokusAwal() { }

    /** Dipanggil saat tombol utama diklik (pesan error sudah dibersihkan). */
    protected abstract void simpan();

    protected void tutup()           { penutup.run(); }
    protected void ukuranBerubah()   { perbaruiUkuran.run(); }

    protected void tampilkanError(String pesan) {
        lblError.setText(pesan);
    }

    protected void infoBerhasil(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void infoGagal(String judulDialog, String pesan) {
        JOptionPane.showMessageDialog(this, pesan, judulDialog, JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================================
    // Helper kolom isian
    // =========================================================================

    /** Label di atas + komponen input di bawahnya. x = kolom (0/1), y = baris label, span = lebar kolom. */
    protected void tambahField(String label, JComponent comp, int x, int y, int span) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(NAVY);
        tempatkan(l, x, y, span, 12);
        tempatkan(comp, x, y + 1, span, 4);
    }

    protected void tempatkan(Component c, int x, int y, int span, int topGap) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = span;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(topGap, x == 0 ? 0 : 14, 0, 0);
        isi.add(c, g);
    }

    protected JTextField inputTeks(String placeholder) {
        JTextField f = new JTextField();
        gayaInput(f, placeholder);
        return f;
    }

    protected JTextField inputBaca(String teks) {
        JTextField f = new JTextField(teks);
        gayaInput(f, null);
        f.setEditable(false);
        f.setBackground(new Color(245, 245, 245));
        f.setForeground(GRAY);
        return f;
    }

    protected void gayaInput(JTextField f, String placeholder) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(0, 40));
        if (placeholder != null) f.putClientProperty("JTextField.placeholderText", placeholder); // efek FlatLaf
        f.putClientProperty("JComponent.roundRect", true);                                       // efek FlatLaf
    }

    protected void gayaCombo(JComboBox<?> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setPreferredSize(new Dimension(0, 40));
        c.putClientProperty("JComponent.roundRect", true);
    }

    protected JSpinner spinnerAngka(int awal, int min, int maks, int langkah) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(awal, min, maks, langkah));
        s.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        s.setPreferredSize(new Dimension(0, 40));
        return s;
    }

    /** Kotak ringkasan abu-abu dengan baris "judul : nilai". Mengembalikan panelnya. */
    protected RoundedPanel kotakRingkasan() {
        RoundedPanel card = new RoundedPanel(16, CARD_BG);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        return card;
    }

    protected void barisRingkasan(JPanel card, String judulBaris, JLabel nilai, int row) {
        int gap = (row == 0) ? 0 : 8;

        JLabel t = new JLabel(judulBaris);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(GRAY);
        t.setPreferredSize(new Dimension(120, t.getPreferredSize().height));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = row;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(gap, 0, 0, 12);
        card.add(t, g);

        g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = row;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(gap, 0, 0, 0);
        card.add(nilai, g);
    }

    protected JLabel labelNilai(boolean tebal) {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("Segoe UI", tebal ? Font.BOLD : Font.PLAIN, 12));
        l.setForeground(new Color(30, 30, 30));
        return l;
    }
}
