package fitmanage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Form "Check-In Hari Ini" (kotak pop-up).
 * Alur: cari member (ID / nama) -> pilih -> cek status paket -> klik Check-In.
 *
 * Aturan:
 *  - Paket kedaluwarsa  -> check-in ditolak
 *  - Sudah check-in hari ini -> check-in ditolak
 *  - Paket hampir habis -> boleh check-in, tampil peringatan
 *
 * Catatan: memakai class RoundedPanel yang ada di Dashboard.java (satu package).
 */
public class CheckInDialog extends JDialog {

    private static final Color NAVY    = new Color(10, 25, 60);
    private static final Color YELLOW  = new Color(230, 179, 51);
    private static final Color RED     = new Color(220, 53, 69);
    private static final Color GREEN   = new Color(25, 135, 84);
    private static final Color ORANGE  = new Color(214, 122, 0);
    private static final Color GRAY    = new Color(100, 100, 100);
    private static final Color CARD_BG = new Color(245, 247, 250);

    private static final Locale ID = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter FORMAT_HARI    = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", ID);
    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("dd MMMM yyyy", ID);
    private static final DateTimeFormatter FORMAT_JAM     = DateTimeFormatter.ofPattern("HH:mm");

    private final DataStore store = DataStore.getInstance();

    private JTextField txtCari;
    private DefaultListModel<Member> listModel;
    private JList<Member> listMember;
    private JLabel lblNama, lblInfo, lblStatus, lblMasa, lblExtra, lblPesan;
    private JButton btnCheckIn;
    private Member terpilih;

    public CheckInDialog(Frame owner) {
        super(owner, "Check-In Hari Ini", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        buildUi();
        muatDaftar();

        pack();
        setSize(Math.max(getWidth(), 540), getHeight());
        setLocationRelativeTo(owner);

        // Esc = tutup
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        SwingUtilities.invokeLater(() -> txtCari.requestFocusInWindow());
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
        JLabel lblTitle = new JLabel("Check-In Hari Ini");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(NAVY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblTanggal = new JLabel(LocalDate.now().format(FORMAT_HARI));
        lblTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTanggal.setForeground(GRAY);
        lblTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(lblTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(lblTanggal);

        root.add(header, BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildContent() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        // --- Pencarian ---
        JLabel lblCari = new JLabel("Cari Member (ID atau Nama)");
        lblCari.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCari.setForeground(NAVY);

        txtCari = new JTextField();
        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCari.setPreferredSize(new Dimension(0, 40));
        txtCari.putClientProperty("JTextField.placeholderText", "Ketik ID (mis. MEM-001) atau nama member"); // efek FlatLaf
        txtCari.putClientProperty("JComponent.roundRect", true);                                              // efek FlatLaf

        // --- Daftar hasil pencarian ---
        listModel = new DefaultListModel<>();
        listMember = new JList<>(listModel);
        listMember.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listMember.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listMember.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Member) {
                    Member m = (Member) value;
                    l.setText(m.getIdMember() + "     " + m.getNama() + "   (" + m.getJenisMember() + ")");
                }
                l.setBorder(new EmptyBorder(6, 10, 6, 10));
                return l;
            }
        });
        listMember.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pilih(listMember.getSelectedValue());
        });

        JScrollPane scroll = new JScrollPane(listMember);
        scroll.setBorder(new LineBorder(new Color(225, 225, 225)));
        scroll.setPreferredSize(new Dimension(0, 140));

        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { muatDaftar(); }
            @Override public void removeUpdate(DocumentEvent e)  { muatDaftar(); }
            @Override public void changedUpdate(DocumentEvent e) { muatDaftar(); }
        });

        // --- Kotak detail member terpilih ---
        RoundedPanel card = new RoundedPanel(16, CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setPreferredSize(new Dimension(0, 142));

        lblNama = new JLabel(" ");
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblNama.setForeground(NAVY);
        lblInfo = new JLabel(" ");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(GRAY);
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMasa = new JLabel(" ");
        lblMasa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMasa.setForeground(GRAY);
        lblExtra = new JLabel(" ");
        lblExtra.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblExtra.setForeground(GRAY);
        for (JLabel l : new JLabel[]{lblNama, lblInfo, lblStatus, lblMasa, lblExtra}) {
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        card.add(lblNama);
        card.add(lblInfo);
        card.add(Box.createVerticalStrut(8));
        card.add(lblStatus);
        card.add(Box.createVerticalStrut(2));
        card.add(lblMasa);
        card.add(Box.createVerticalStrut(2));
        card.add(lblExtra);

        // --- Pesan hasil (peringatan / berhasil) ---
        lblPesan = new JLabel(" ");
        lblPesan.setFont(new Font("Segoe UI", Font.BOLD, 12));

        place(p, lblCari, 0, 0);
        place(p, txtCari, 1, 6);
        place(p, scroll, 2, 8);
        place(p, card, 3, 12);
        place(p, lblPesan, 4, 10);
        return p;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        p.setOpaque(false);

        JButton btnTutup = new JButton("Tutup");
        btnTutup.setBackground(new Color(240, 240, 240));
        btnTutup.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTutup.setPreferredSize(new Dimension(110, 44));
        btnTutup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTutup.addActionListener(e -> dispose());

        btnCheckIn = new JButton("Check-In Sekarang");
        btnCheckIn.setBackground(YELLOW);
        btnCheckIn.setForeground(Color.WHITE);
        btnCheckIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCheckIn.setPreferredSize(new Dimension(180, 44));
        btnCheckIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCheckIn.setEnabled(false);
        btnCheckIn.addActionListener(e -> doCheckIn());

        p.add(btnTutup);
        p.add(btnCheckIn);
        getRootPane().setDefaultButton(btnCheckIn); // Enter = Check-In (jika aktif)
        return p;
    }

    private void place(JPanel p, Component c, int row, int topGap) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(topGap, 0, 0, 0);
        p.add(c, g);
    }

    // =========================================================================
    // LOGIKA
    // =========================================================================

    // Isi daftar sesuai kata kunci (maks. 50 hasil)
    private void muatDaftar() {
        listModel.clear();
        int jumlah = 0;
        for (Member m : store.cari(txtCari.getText())) {
            listModel.addElement(m);
            if (++jumlah >= 50) break;
        }
        // Jika hanya satu hasil (mis. ID diketik lengkap), langsung dipilih
        if (listModel.size() == 1) {
            listMember.setSelectedIndex(0);
        } else {
            pilih(null);
        }
    }

    // Menampilkan detail member terpilih dan menentukan apakah boleh check-in
    private void pilih(Member m) {
        terpilih = m;
        btnCheckIn.setEnabled(false);

        if (m == null) {
            lblNama.setText("Belum ada member dipilih");
            lblInfo.setText("Cari lalu pilih member dari daftar di atas.");
            lblStatus.setText(" ");
            lblMasa.setText(" ");
            lblExtra.setText(" ");
            pesan(" ", GRAY);
            return;
        }

        Member.Status status = m.getStatus();
        long sisa = m.getSisaHari();

        lblNama.setText(m.getNama());
        lblInfo.setText(m.getIdMember() + "  |  " + m.getJenisMember());
        lblStatus.setText("Status paket: " + status.getLabel());
        lblStatus.setForeground(status == Member.Status.AKTIF ? GREEN
                : status == Member.Status.HAMPIR_HABIS ? ORANGE : RED);
        String ket = (sisa >= 0) ? sisa + " hari lagi" : "lewat " + (-sisa) + " hari";
        lblMasa.setText("Berlaku sampai " + m.getTanggalBerakhir().format(FORMAT_TANGGAL) + " (" + ket + ")");
        lblExtra.setText(m.getInfoTambahan().isEmpty() ? " " : m.getInfoTambahan());

        Presensi sudah = store.presensiHariIni(m.getIdMember());
        if (status == Member.Status.KEDALUWARSA) {
            pesan("Paket sudah kedaluwarsa. Perpanjang paket sebelum check-in.", RED);
        } else if (sudah != null) {
            pesan("Sudah check-in hari ini pukul " + sudah.getWaktuMasuk().format(FORMAT_JAM) + ".", ORANGE);
        } else {
            btnCheckIn.setEnabled(true);
            if (status == Member.Status.HAMPIR_HABIS) {
                pesan("Perhatian: paket berakhir " + sisa + " hari lagi.", ORANGE);
            } else {
                pesan(" ", GRAY);
            }
        }
    }

    private void doCheckIn() {
        if (terpilih == null) return;
        Member m = terpilih;

        Presensi p = store.catatPresensi(m);
        if (p == null) {          // ditolak oleh aturan (kedaluwarsa / sudah check-in)
            pilih(m);
            return;
        }

        // Reset pencarian dan pilihan supaya siap untuk member berikutnya
        txtCari.setText("");
        muatDaftar();
        listMember.clearSelection();
        pilih(null);
        pesan("Check-in berhasil: " + m.getNama() + " pukul " + p.getWaktuMasuk().format(FORMAT_JAM) + ".", GREEN);
        txtCari.requestFocusInWindow();
    }

    private void pesan(String teks, Color warna) {
        lblPesan.setText(teks);
        lblPesan.setForeground(warna);
    }
}