package fitmanage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Form "Buat Paket" (tombol + Buat Paket di tab Manajemen Paket & Transaksi).
 * Membuat paket langganan (perpanjangan) untuk seorang member:
 * pilih member, durasi, diskon, metode & status pembayaran.
 * Jika status "Lunas", masa berlaku paket member langsung diperpanjang.
 */
class CreatePackageForm extends FormPanel {

    private static final DateTimeFormatter FORMAT_TANGGAL =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"));

    private final DataStore store = DataStore.getInstance();

    private final JTextField txtCari;
    private final DefaultListModel<Member> modelDaftar = new DefaultListModel<>();
    private final JList<Member> daftar = new JList<>(modelDaftar);
    private final JComboBox<String> cmbDurasi;
    private final JSpinner spnDiskon;
    private final JComboBox<String> cmbMetode;
    private final JComboBox<String> cmbStatus;
    private final JLabel lblMember;
    private final JLabel lblBerlaku;
    private final JLabel lblBiaya;
    private final JLabel lblTotal;
    private Member terpilih;

    CreatePackageForm() {
        super("Buat Paket Langganan", "Buat paket baru atau perpanjang paket untuk seorang member.",
                "Simpan Paket", BLUE);

        // --- Pencarian & daftar member ---
        txtCari = inputTeks("Ketik ID (mis. MEM-001) atau nama member");

        daftar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        daftar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        daftar.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Member) {
                    Member m = (Member) value;
                    l.setText(m.getIdMember() + "     " + m.getNama() + "   (" + m.getJenisMember() + ")");
                }
                l.setBorder(new EmptyBorder(5, 10, 5, 10));
                return l;
            }
        });
        JScrollPane scroll = new JScrollPane(daftar);
        scroll.setBorder(new LineBorder(new Color(225, 225, 225)));
        scroll.setPreferredSize(new Dimension(0, 96));

        // --- Pengaturan paket ---
        cmbDurasi = new JComboBox<>(new String[]{"1 Bulan", "3 Bulan", "6 Bulan", "12 Bulan"});
        gayaCombo(cmbDurasi);
        spnDiskon = spinnerAngka(0, 0, 50, 5);
        cmbMetode = new JComboBox<>(Transaksi.METODE);
        gayaCombo(cmbMetode);
        cmbStatus = new JComboBox<>(new String[]{"Lunas", "Pending"});
        gayaCombo(cmbStatus);

        tambahField("Cari Member (ID atau Nama)", txtCari, 0, 0, 2);
        tempatkan(scroll, 0, 2, 2, 8);
        tambahField("Durasi Paket", cmbDurasi, 0, 3, 1);
        tambahField("Diskon (%)", spnDiskon, 1, 3, 1);
        tambahField("Metode Pembayaran", cmbMetode, 0, 5, 1);
        tambahField("Status Pembayaran", cmbStatus, 1, 5, 1);

        // --- Ringkasan ---
        RoundedPanel kartu = kotakRingkasan();
        lblMember = labelNilai(true);
        lblBerlaku = labelNilai(false);
        lblBiaya = labelNilai(false);
        lblTotal = new JLabel(" ");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(BLUE);
        barisRingkasan(kartu, "Member", lblMember, 0);
        barisRingkasan(kartu, "Berlaku sampai", lblBerlaku, 1);
        barisRingkasan(kartu, "Biaya paket", lblBiaya, 2);
        barisRingkasan(kartu, "Total Bayar", lblTotal, 3);
        tempatkan(kartu, 0, 7, 2, 16);

        // --- Reaksi ---
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { muatDaftar(); }
            @Override public void removeUpdate(DocumentEvent e)  { muatDaftar(); }
            @Override public void changedUpdate(DocumentEvent e) { muatDaftar(); }
        });
        daftar.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pilih(daftar.getSelectedValue());
        });
        cmbDurasi.addActionListener(e -> perbaruiRingkasan());
        cmbStatus.addActionListener(e -> perbaruiRingkasan());
        spnDiskon.addChangeListener(e -> perbaruiRingkasan());

        muatDaftar();
    }

    @Override
    void fokusAwal() {
        txtCari.requestFocusInWindow();
    }

    // Isi daftar sesuai kata kunci (maks. 50 hasil)
    private void muatDaftar() {
        modelDaftar.clear();
        int jumlah = 0;
        for (Member m : store.cari(txtCari.getText())) {
            modelDaftar.addElement(m);
            if (++jumlah >= 50) break;
        }
        if (modelDaftar.size() == 1) {
            daftar.setSelectedIndex(0);   // hasil tunggal langsung dipilih
        } else {
            pilih(null);
        }
    }

    private void pilih(Member m) {
        terpilih = m;
        perbaruiRingkasan();
    }

    private int durasiBulan() {
        return Integer.parseInt(((String) cmbDurasi.getSelectedItem()).split(" ")[0]);
    }

    private long hitungTotal(long biaya, int diskon) {
        return Math.round(biaya * (100 - diskon) / 100.0);
    }

    private void perbaruiRingkasan() {
        if (terpilih == null) {
            lblMember.setText("Belum ada member dipilih");
            lblBerlaku.setText("-");
            lblBiaya.setText("-");
            lblTotal.setText("-");
            return;
        }
        int bulan = durasiBulan();
        int diskon = (Integer) spnDiskon.getValue();
        long biaya = terpilih.hitungBiayaPerpanjangan(bulan);

        LocalDate mulai = terpilih.isAktif() ? terpilih.getTanggalBerakhir() : LocalDate.now();
        boolean lunas = "Lunas".equals(cmbStatus.getSelectedItem());

        lblMember.setText(terpilih.getNama() + " (" + terpilih.getIdMember() + ", " + terpilih.getJenisMember() + ")");
        lblBerlaku.setText(mulai.plusMonths(bulan).format(FORMAT_TANGGAL) + (lunas ? "" : "  (setelah dilunasi)"));
        lblBiaya.setText(Member.formatRupiah(biaya));
        lblTotal.setText(Member.formatRupiah(hitungTotal(biaya, diskon)));
    }

    @Override
    protected void simpan() {
        if (terpilih == null) {
            tampilkanError("Pilih member terlebih dahulu dari daftar.");
            txtCari.requestFocusInWindow();
            return;
        }

        int bulan = durasiBulan();
        int diskon = (Integer) spnDiskon.getValue();
        String status = (String) cmbStatus.getSelectedItem();

        Transaksi t;
        try {
            t = store.buatTransaksi(terpilih, bulan, diskon, (String) cmbMetode.getSelectedItem(), status);
        } catch (DataStore.DataException ex) {
            infoGagal("Gagal Menyimpan", ex.getMessage());
            return;
        }

        String catatan = t.isLunas()
                ? "Paket member diperpanjang sampai " + terpilih.getTanggalBerakhir().format(FORMAT_TANGGAL) + "."
                : "Status Pending: paket member belum diperpanjang sampai dilunasi.";
        infoBerhasil("Paket berhasil dibuat.\n\nTransaksi : " + t.getIdTransaksi()
                + "\nMember : " + t.getNamaMember()
                + "\nPaket : " + t.getPaketTeks()
                + "\nTotal : " + Member.formatRupiah(t.getTotal())
                + "\nStatus : " + t.getStatus()
                + "\n\n" + catatan);
        tutup();
    }
}
