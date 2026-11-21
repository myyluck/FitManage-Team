package fitmanage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Form "Tambah Member" (kotak pop-up). Satu form yang sama dipakai di
 * tab Dashboard dan tab Data Member.
 *
 * Tipe member yang dipilih menentukan kelas yang dibuat:
 *   Reguler -> MemberRegular | VIP -> MemberVIP | Personal Trainer -> PersonalTrainer
 *
 * Catatan: memakai class RoundedPanel yang ada di Dashboard.java (satu package).
 */
public class AddMemberDialog extends JDialog {

    private static final Color NAVY    = new Color(10, 25, 60);
    private static final Color BLUE    = new Color(0, 131, 176);
    private static final Color RED     = new Color(220, 53, 69);
    private static final Color GRAY    = new Color(100, 100, 100);
    private static final Color CARD_BG = new Color(245, 247, 250);

    private static final String TIPE_REGULER = "Reguler";
    private static final String TIPE_VIP     = "VIP";
    private static final String TIPE_PT      = "Personal Trainer";
    private static final String[] DAFTAR_TIPE   = {TIPE_REGULER, TIPE_VIP, TIPE_PT};
    private static final String[] DAFTAR_DURASI = {"1 Bulan", "3 Bulan", "6 Bulan", "12 Bulan"};

    private static final int LEBAR_MIN = 540;

    private static final DateTimeFormatter FORMAT_TANGGAL =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"));

    private final DataStore store = DataStore.getInstance();

    private JTextField txtId, txtTanggal, txtNama, txtHp, txtLoker;
    private JComboBox<String> cmbTipe, cmbDurasi, cmbTrainer;
    private JSpinner spnSesi;
    private CardLayout extraCards;
    private JPanel extraPanel;
    private JLabel lblBerlaku, lblBenefit, lblTotal, lblError;
    private JButton btnSimpan;

    public AddMemberDialog(Frame owner) {
        super(owner, "Tambah Member", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        buildUi();
        aturBagianTambahan();
        updatePreview();

        pack();
        setSize(Math.max(getWidth(), LEBAR_MIN), getHeight());
        setLocationRelativeTo(owner);

        // Esc = tutup
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingUtilities.invokeLater(() -> txtNama.requestFocusInWindow());
    }

    // =========================================================================
    // MEMBANGUN TAMPILAN
    // =========================================================================
    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(22, 30, 22, 30));
        setContentPane(root);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("Tambah Member Baru");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(NAVY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblSub = new JLabel("Lengkapi data berikut untuk mendaftarkan anggota gym.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(GRAY);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(lblTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(lblSub);

        root.add(header, BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        txtId = readOnlyField(store.generateId());
        txtTanggal = readOnlyField(LocalDate.now().format(FORMAT_TANGGAL));

        txtNama = new JTextField();
        styleInput(txtNama, "Nama sesuai identitas");
        txtHp = new JTextField();
        styleInput(txtHp, "Contoh: 081234567890");

        // Kolom No. HP hanya menerima angka (maks. 13 digit)
        txtHp.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                boolean kontrol = (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE);
                if (!kontrol && (!Character.isDigit(c) || txtHp.getText().length() >= 13)) {
                    e.consume();
                }
                lblError.setText(" ");
            }
        });
        txtNama.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                lblError.setText(" ");
            }
        });

        cmbTipe = new JComboBox<>(DAFTAR_TIPE);
        styleCombo(cmbTipe);
        cmbDurasi = new JComboBox<>(DAFTAR_DURASI);
        styleCombo(cmbDurasi);

        // Bagian tambahan yang berbeda tiap tipe member
        extraCards = new CardLayout();
        extraPanel = new JPanel(extraCards);
        extraPanel.setOpaque(false);

        JPanel kosong = new JPanel();
        kosong.setOpaque(false);
        extraPanel.add(kosong, TIPE_REGULER);

        JPanel vip = new JPanel(new GridBagLayout());
        vip.setOpaque(false);
        txtLoker = new JTextField();
        styleInput(txtLoker, "Contoh: A-12 (boleh dikosongkan)");
        addField(vip, "No. Loker (opsional)", txtLoker, 0, 0, 2);
        extraPanel.add(vip, TIPE_VIP);

        JPanel pt = new JPanel(new GridBagLayout());
        pt.setOpaque(false);
        cmbTrainer = new JComboBox<>(store.getDaftarTrainer());
        styleCombo(cmbTrainer);
        spnSesi = new JSpinner(new SpinnerNumberModel(8, 1, 48, 1));
        spnSesi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spnSesi.setPreferredSize(new Dimension(0, 40));
        addField(pt, "Trainer", cmbTrainer, 0, 0, 1);
        addField(pt, "Jumlah Sesi", spnSesi, 1, 0, 1);
        extraPanel.add(pt, TIPE_PT);

        // Hitung ulang ringkasan setiap ada perubahan
        cmbTipe.addActionListener(e -> {
            aturBagianTambahan();
            updatePreview();
            repack();
        });
        cmbDurasi.addActionListener(e -> updatePreview());
        spnSesi.addChangeListener(e -> updatePreview());

        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(RED);

        addField(form, "ID Member", txtId, 0, 0, 1);
        addField(form, "Tanggal Daftar", txtTanggal, 1, 0, 1);
        addField(form, "Nama Lengkap", txtNama, 0, 2, 2);
        addField(form, "No. HP", txtHp, 0, 4, 2);
        addField(form, "Tipe Member", cmbTipe, 0, 6, 1);
        addField(form, "Durasi Paket", cmbDurasi, 1, 6, 1);
        place(form, extraPanel, 0, 8, 2, 0);
        place(form, buildSummary(), 0, 9, 2, 16);
        place(form, lblError, 0, 10, 2, 8);
        return form;
    }

    // Kotak ringkasan: masa berlaku, benefit, dan total bayar
    private JComponent buildSummary() {
        RoundedPanel card = new RoundedPanel(16, CARD_BG);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        lblBerlaku = new JLabel();
        lblBerlaku.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBerlaku.setForeground(new Color(30, 30, 30));

        lblBenefit = new JLabel();
        lblBenefit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBenefit.setForeground(new Color(30, 30, 30));

        lblTotal = new JLabel();
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(BLUE);

        summaryRow(card, "Berlaku sampai", lblBerlaku, 0);
        summaryRow(card, "Benefit", lblBenefit, 1);
        summaryRow(card, "Total Bayar", lblTotal, 2);
        return card;
    }

    private void summaryRow(JPanel card, String judul, JLabel nilai, int row) {
        int gap = (row == 0) ? 0 : 8;

        JLabel t = new JLabel(judul);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(GRAY);
        t.setPreferredSize(new Dimension(100, t.getPreferredSize().height));
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

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        p.setOpaque(false);

        JButton btnBatal = new JButton("Batal");
        btnBatal.setBackground(new Color(240, 240, 240));
        btnBatal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBatal.setPreferredSize(new Dimension(110, 44));
        btnBatal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBatal.addActionListener(e -> dispose());

        btnSimpan = new JButton("Simpan Member");
        btnSimpan.setBackground(BLUE);
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSimpan.setPreferredSize(new Dimension(170, 44));
        btnSimpan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSimpan.addActionListener(e -> simpan());

        p.add(btnBatal);
        p.add(btnSimpan);
        getRootPane().setDefaultButton(btnSimpan); // Enter = Simpan
        return p;
    }

    // ---------------------------------------------------------------
    // Helper tampilan
    // ---------------------------------------------------------------

    // Label di atas + komponen input di bawahnya
    private void addField(JPanel p, String label, JComponent comp, int x, int y, int span) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(NAVY);
        place(p, l, x, y, span, 12);
        place(p, comp, x, y + 1, span, 4);
    }

    private void place(JPanel p, Component c, int x, int y, int span, int topGap) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = span;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(topGap, x == 0 ? 0 : 14, 0, 0);
        p.add(c, g);
    }

    private JTextField readOnlyField(String isi) {
        JTextField f = new JTextField(isi);
        styleInput(f, null);
        f.setEditable(false);
        f.setBackground(new Color(245, 245, 245));
        f.setForeground(GRAY);
        return f;
    }

    private void styleInput(JTextField f, String placeholder) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(0, 40));
        if (placeholder != null) f.putClientProperty("JTextField.placeholderText", placeholder); // efek FlatLaf
        f.putClientProperty("JComponent.roundRect", true);                                       // efek FlatLaf
    }

    private void styleCombo(JComboBox<?> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setPreferredSize(new Dimension(0, 40));
        c.putClientProperty("JComponent.roundRect", true);
    }

    // Tampilkan/sembunyikan bagian tambahan sesuai tipe member
    private void aturBagianTambahan() {
        String tipe = (String) cmbTipe.getSelectedItem();
        extraCards.show(extraPanel, tipe);
        extraPanel.setVisible(!TIPE_REGULER.equals(tipe));
    }

    // Ukuran dialog menyesuaikan isi (dipanggil saat tipe member berganti)
    private void repack() {
        pack();
        setSize(Math.max(getWidth(), LEBAR_MIN), getHeight());
    }

    // =========================================================================
    // LOGIKA FORM
    // =========================================================================

    /** Membuat objek member sesuai tipe yang dipilih (polymorphism). */
    private Member buatMember() {
        String id = txtId.getText();
        String nama = txtNama.getText().trim();
        String hp = txtHp.getText().trim();
        LocalDate hariIni = LocalDate.now();
        int durasi = Integer.parseInt(((String) cmbDurasi.getSelectedItem()).split(" ")[0]);

        switch ((String) cmbTipe.getSelectedItem()) {
            case TIPE_VIP:
                return new MemberVIP(id, nama, hp, hariIni, durasi, txtLoker.getText().trim());
            case TIPE_PT: {
                String trainer = (String) cmbTrainer.getSelectedItem();
                int sesi = (Integer) spnSesi.getValue();
                return new PersonalTrainer(id, nama, hp, hariIni, durasi, trainer == null ? "-" : trainer, sesi);
            }
            default:
                return new MemberRegular(id, nama, hp, hariIni, durasi);
        }
    }

    // Memperbarui kotak ringkasan (masa berlaku, benefit, total bayar)
    private void updatePreview() {
        Member m = buatMember();
        lblBerlaku.setText(m.getTanggalBerakhir().format(FORMAT_TANGGAL));
        lblBenefit.setText("<html><div style='width:240px'>" + m.getBenefit() + "</div></html>");
        lblTotal.setText(Member.formatRupiah(m.hitungTotalBayar()));
    }

    // Mengembalikan pesan error, atau null jika data valid
    private String validasi() {
        String nama = txtNama.getText().trim();
        String hp = txtHp.getText().trim();

        if (nama.isEmpty()) {
            txtNama.requestFocusInWindow();
            return "Nama lengkap wajib diisi.";
        }
        if (nama.length() < 3 || !nama.matches("[\\p{L} .'-]+")) {
            txtNama.requestFocusInWindow();
            return "Nama minimal 3 karakter dan hanya berisi huruf.";
        }
        if (hp.isEmpty()) {
            txtHp.requestFocusInWindow();
            return "No. HP wajib diisi.";
        }
        if (!hp.matches("0\\d{9,12}")) {
            txtHp.requestFocusInWindow();
            return "No. HP harus 10-13 digit dan diawali 0.";
        }
        if (store.adaNoHp(hp)) {
            txtHp.requestFocusInWindow();
            return "No. HP ini sudah terdaftar.";
        }
        if (TIPE_PT.equals(cmbTipe.getSelectedItem()) && cmbTrainer.getSelectedItem() == null) {
            return "Belum ada trainer aktif yang bisa dipilih.";
        }
        return null;
    }

    private void simpan() {
        String error = validasi();
        if (error != null) {
            lblError.setText(error);
            return;
        }

        Member member = buatMember();
        store.tambahMember(member);

        JOptionPane.showMessageDialog(this,
                "Member berhasil ditambahkan.\n\n"
                        + "ID           : " + member.getIdMember() + "\n"
                        + "Nama      : " + member.getNama() + "\n"
                        + "Tipe        : " + member.getJenisMember() + "\n"
                        + "Total bayar : " + Member.formatRupiah(member.hitungTotalBayar()),
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}