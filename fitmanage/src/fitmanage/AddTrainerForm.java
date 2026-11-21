package fitmanage;

import javax.swing.JComboBox;
import javax.swing.JTextField;

/** Form "Tambah Trainer" (tombol + Tambah Trainer di tab Manajemen Trainer). */
class AddTrainerForm extends FormPanel {

    private static final String[] SPESIALISASI = {
        "Zumba & Aerobic", "Yoga & Pilates", "Bodybuilding", "Strength & Conditioning",
        "Boxing & Cardio", "Crossfit", "HIIT", "Spinning", "Functional Training",
        "Weight Loss Program", "Personal Training"
    };

    private final DataStore store = DataStore.getInstance();
    private final JTextField txtNama;
    private final JTextField txtHp;
    private final JComboBox<String> cmbSpesialisasi;
    private final JComboBox<String> cmbStatus;

    AddTrainerForm() {
        super("Tambah Trainer Baru", "Lengkapi data pelatih yang akan bergabung.", "Simpan Trainer", BLUE);

        JTextField txtId = inputBaca(store.generateIdTrainer());
        txtNama = inputTeks("Nama sesuai identitas");
        txtHp = inputTeks("Contoh: 081234567890");

        cmbSpesialisasi = new JComboBox<>(SPESIALISASI);
        cmbSpesialisasi.setEditable(true);          // boleh mengetik spesialisasi lain
        gayaCombo(cmbSpesialisasi);
        cmbStatus = new JComboBox<>(new String[]{"Aktif", "Cuti"});
        gayaCombo(cmbStatus);

        tambahField("ID Trainer", txtId, 0, 0, 1);
        tambahField("Status", cmbStatus, 1, 0, 1);
        tambahField("Nama Lengkap", txtNama, 0, 2, 2);
        tambahField("No. HP", txtHp, 0, 4, 2);
        tambahField("Spesialisasi (boleh ketik sendiri)", cmbSpesialisasi, 0, 6, 2);
    }

    @Override
    void fokusAwal() {
        txtNama.requestFocusInWindow();
    }

    private String spesialisasi() {
        Object nilai = cmbSpesialisasi.getEditor().getItem();
        return nilai == null ? "" : nilai.toString().trim();
    }

    @Override
    protected void simpan() {
        String nama = txtNama.getText().trim();
        String hp = txtHp.getText().trim();
        String spes = spesialisasi();

        if (nama.length() < 3 || !nama.matches("[\\p{L} .'-]+")) {
            tampilkanError("Nama minimal 3 karakter dan hanya berisi huruf.");
            txtNama.requestFocusInWindow();
            return;
        }
        if (!hp.matches("0\\d{9,12}")) {
            tampilkanError("No. HP harus 10-13 digit angka dan diawali 0.");
            txtHp.requestFocusInWindow();
            return;
        }
        if (store.adaNoHpTrainer(hp)) {
            tampilkanError("No. HP ini sudah dipakai trainer lain.");
            txtHp.requestFocusInWindow();
            return;
        }
        if (spes.isEmpty()) {
            tampilkanError("Spesialisasi wajib diisi.");
            return;
        }

        String[] baris;
        try {
            baris = store.tambahTrainer(nama, spes, hp, (String) cmbStatus.getSelectedItem());
        } catch (DataStore.DataException ex) {
            infoGagal("Gagal Menyimpan", ex.getMessage());
            return;
        }
        infoBerhasil("Trainer berhasil ditambahkan.\n\nID : " + baris[0] + "\nNama : " + baris[1]
                + "\nSpesialisasi : " + baris[2] + "\nStatus : " + baris[4]);
        tutup();
    }
}
