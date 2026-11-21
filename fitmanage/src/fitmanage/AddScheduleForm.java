package fitmanage;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JComponent; // <--- Tambahin baris ini
import java.time.LocalTime;

/** Form "Tambah Jadwal" (tombol + Tambah Jadwal di tab Kelas & Jadwal). */
class AddScheduleForm extends FormPanel {

    private final DataStore store = DataStore.getInstance();
    private final JTextField txtKelas;
    private final JComboBox<String> cmbInstruktur;
    private final JComboBox<String> cmbHari;
    private final JComboBox<String> cmbJam;
    private final JComboBox<String> cmbDurasi;
    private final JSpinner spnKapasitas;
    private final JLabel lblJadwal;
    private final JLabel lblInfo;

    AddScheduleForm() {
        super("Tambah Jadwal Kelas", "Atur kelas kebugaran baru beserta instruktur dan waktunya.",
                "Simpan Jadwal", BLUE);

        txtKelas = inputTeks("Contoh: Zumba Dance");

        cmbInstruktur = new JComboBox<>(store.getDaftarTrainer());
        gayaCombo(cmbInstruktur);

        cmbHari = new JComboBox<>(Jadwal.HARI);
        gayaCombo(cmbHari);

       // Jam mulai 10:00 - 00:00 (12 malam) setiap 30 menit
        java.util.List<String> jam = new java.util.ArrayList<>();
        for (int menit = 10 * 60; menit <= 24 * 60; menit += 30) {
            //pakai % 24 biar pas angkanya 24, dia balik jadi 0 (jam 00:00)
            int jamKe = (menit / 60) % 24; 
            jam.add(LocalTime.of(jamKe, menit % 60).toString());
        }
        cmbJam = new JComboBox<>(jam.toArray(new String[0]));
        cmbJam.setSelectedItem("16:00");
        gayaCombo(cmbJam);

        cmbDurasi = new JComboBox<>(new String[]{"45 Menit", "60 Menit", "90 Menit"});
        cmbDurasi.setSelectedItem("60 Menit");
        gayaCombo(cmbDurasi);

 spnKapasitas = spinnerAngka(20, 1, 100, 1);

        // Pakai nama variabel spesifik biar nggak bentrok
        JComponent editorKapasitas = spnKapasitas.getEditor();
        if (editorKapasitas instanceof JSpinner.DefaultEditor) {
            JTextField tfKapasitas = ((JSpinner.DefaultEditor) editorKapasitas).getTextField();
            tfKapasitas.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void updateModel() {
                    try {
                        String text = tfKapasitas.getText().trim();
                        if (!text.isEmpty()) {
                            int val = Integer.parseInt(text);
                            if (val >= 5 && val <= 100) { // Cek batas min/max
                                spnKapasitas.setValue(val);
                            }
                        }
                    } catch (Exception ex) {
                        // Abaikan kalau pas dihapus jadi kosong
                    }
                }
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            });
        }

        tambahField("Nama Kelas", txtKelas, 0, 0, 2);
        tambahField("Instruktur", cmbInstruktur, 0, 2, 2);
        tambahField("Hari", cmbHari, 0, 4, 1);
        tambahField("Jam Mulai", cmbJam, 1, 4, 1);
        tambahField("Durasi", cmbDurasi, 0, 6, 1);
        tambahField("Kapasitas (orang)", spnKapasitas, 1, 6, 1);

        // Ringkasan jadwal
        RoundedPanel kartu = kotakRingkasan();
        lblJadwal = labelNilai(true);
        lblInfo = labelNilai(false);
        barisRingkasan(kartu, "Jadwal", lblJadwal, 0);
        barisRingkasan(kartu, "Keterangan", lblInfo, 1);
        tempatkan(kartu, 0, 8, 2, 16);

        cmbHari.addActionListener(e -> perbaruiRingkasan());
       cmbJam.addActionListener(e -> {
    try {
        spnKapasitas.commitEdit(); // Paksa simpan angka spinner dulu sebelum ringkasan diperbarui
    } catch (java.text.ParseException ex) {
        // Abaikan kalau formatnya kosong/salah
    }
    perbaruiRingkasan();
});
        cmbDurasi.addActionListener(e -> perbaruiRingkasan());
        cmbInstruktur.addActionListener(e -> perbaruiRingkasan());
        spnKapasitas.addChangeListener(e -> perbaruiRingkasan());
        perbaruiRingkasan();
    }

    @Override
    void fokusAwal() {
        txtKelas.requestFocusInWindow();
    }

    private int durasiMenit() {
        return Integer.parseInt(((String) cmbDurasi.getSelectedItem()).split(" ")[0]);
    }

    private Jadwal buatJadwal() {
        String pilihan = (String) cmbInstruktur.getSelectedItem();
        // Ambil nama murninya saja sebelum tanda kurung " ("
        String instruktur = (pilihan == null) ? "-" : pilihan.split(" \\(")[0].trim();

        return new Jadwal(txtKelas.getText().trim(), instruktur,
                (String) cmbHari.getSelectedItem(), LocalTime.parse((String) cmbJam.getSelectedItem()),
                durasiMenit(), (Integer) spnKapasitas.getValue(), 0);
    }

    private void perbaruiRingkasan() {
        Jadwal j = buatJadwal();
        lblJadwal.setText(j.getHariJamTeks());
        lblInfo.setText(j.getInstruktur() + "  |  kapasitas " + j.getKapasitas() + " orang");
    }

    @Override
    protected void simpan() {
        String kelas = txtKelas.getText().trim();
        if (kelas.length() < 3) {
            tampilkanError("Nama kelas minimal 3 karakter.");
            txtKelas.requestFocusInWindow();
            return;
        }
        if (cmbInstruktur.getSelectedItem() == null) {
            tampilkanError("Belum ada trainer aktif yang bisa dipilih sebagai instruktur.");
            return;
        }

        Jadwal baru = buatJadwal();
        Jadwal bentrok = store.cariBentrok(baru);
        if (bentrok != null) {
            tampilkanError(baru.getInstruktur() + " sudah mengajar \"" + bentrok.getNamaKelas() + "\" pada "
                    + bentrok.getHariJamTeks() + ".");
            return;
        }

        store.tambahJadwal(baru);
        infoBerhasil("Jadwal berhasil ditambahkan.\n\nKelas : " + baru.getNamaKelas()
                + "\nInstruktur : " + baru.getInstruktur()
                + "\nWaktu : " + baru.getHariJamTeks()
                + "\nKapasitas : " + baru.getKapasitas() + " orang");
        tutup();
    }
}
