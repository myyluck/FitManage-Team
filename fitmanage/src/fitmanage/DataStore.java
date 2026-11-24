package fitmanage;

import fitmanage.dao.JadwalDAO;
import fitmanage.dao.JadwalDAOImpl;
import fitmanage.dao.MemberDAO;
import fitmanage.dao.MemberDAOImpl;
import fitmanage.dao.PresensiDAO;
import fitmanage.dao.PresensiDAOImpl;
import fitmanage.dao.TrainerDAO;
import fitmanage.dao.TrainerDAOImpl;
import fitmanage.dao.TransaksiDAO;
import fitmanage.dao.TransaksiDAOImpl;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Penyimpanan data aplikasi di memori (Singleton & Observer).
 * Seluruh akses database didelegasikan ke package fitmanage.dao.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    public static DataStore getInstance() {
        return INSTANCE;
    }

    /** Pendengar perubahan data (dipakai Dashboard untuk memperbarui tabel). */
    public interface Listener {
        default void onMemberAdded(Member member) {}
        default void onMemberUpdated() {}
        default void onCheckIn(Presensi presensi) {}
    }

    // Objek DAO untuk masing-masing tabel Supabase
    private final MemberDAO memberDAO       = new MemberDAOImpl();
    private final PresensiDAO presensiDAO   = new PresensiDAOImpl();
    private final JadwalDAO jadwalDAO       = new JadwalDAOImpl();
    private final TrainerDAO trainerDAO     = new TrainerDAOImpl();
    private final TransaksiDAO transaksiDAO = new TransaksiDAOImpl();

    private final List<Member> members            = new ArrayList<>();
    private final List<Presensi> presensi         = new ArrayList<>();
    private final List<Jadwal> daftarJadwal       = new ArrayList<>();
    private final List<String[]> daftarTrainer    = new ArrayList<>();
    private final List<Transaksi> daftarTransaksi = new ArrayList<>();
    private final List<Listener> listeners        = new ArrayList<>();

   private DataStore() {
        muatDataDariDB();
    }

    public void muatDataDariDB() {
        members.clear();
        presensi.clear();

        try {
            members.addAll(memberDAO.getAll());
            presensi.addAll(presensiDAO.getAll(members));
            System.out.println("[Sistem] Berhasil sinkronisasi " + members.size() + " member & "
                    + presensi.size() + " total riwayat presensi dari Supabase!");
        } catch (SQLException e) {
            System.err.println("[Error] Gagal narik data dari Supabase: " + e.getMessage());
        }

        muatTransaksiDariDB();
        sinkronkanMasaAktifDariTransaksi();
        muatTrainerDariDB();
    }

    private void sinkronkanMasaAktifDariTransaksi() {
        // Putar dari transaksi paling lama ke paling baru (daftarTransaksi urut DESC)
        for (int i = daftarTransaksi.size() - 1; i >= 0; i--) {
            Transaksi trx = daftarTransaksi.get(i);
            if (!trx.isLunas()) continue;

            for (Member m : members) {
                if (m.getIdMember().equals(trx.getIdMember())) {
                    LocalDate tglTrx = (trx.getWaktu() != null)
                            ? trx.getWaktu().toLocalDate() : LocalDate.now();
                    m.perpanjangPaket(trx.getDurasiBulan(), tglTrx);
                    break;
                }
            }
        }

        // Pastikan kolom status di tabel member Supabase ikut sinkron
        for (Member m : members) {
            try {
                memberDAO.update(m);
            } catch (SQLException ignored) {
            }
        }
    }
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    // ---------------------------------------------------------------
    // Member
    // ---------------------------------------------------------------
   public String generateId() {
        int max = 0;
        // 1. Cek dari daftar aktif di memori
        for (Member m : members) {
            try {
                max = Math.max(max, Integer.parseInt(m.getIdMember().substring(4)));
            } catch (RuntimeException ignored) {
            }
        }

        // 2. Cek langsung ke tabel member Supabase (termasuk yang Nonaktif/soft-delete)
        String sql = "SELECT id FROM member WHERE id LIKE 'MEM-%'";
        try (java.sql.Connection conn = fitmanage.dao.KoneksiDB.sambungDB();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                if (id != null && id.length() > 4) {
                    try {
                        max = Math.max(max, Integer.parseInt(id.substring(4)));
                    } catch (RuntimeException ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error] Gagal cek ID terakhir dari Supabase: " + e.getMessage());
        }

        return String.format("MEM-%03d", max + 1);
    }

    public boolean adaNoHp(String noHp) {
        for (Member m : members) {
            if (m.getNoHp().equals(noHp)) {
                return true;
            }
        }
        return false;
    }

    public boolean adaNoHpSelain(String noHp, String idMemberKecuali) {
        for (Member m : members) {
            if (!m.getIdMember().equals(idMemberKecuali) && m.getNoHp().equals(noHp)) {
                return true;
            }
        }
        return false;
    }

    public void tambahMember(Member member) {
        try {
            memberDAO.insert(member);
            System.out.println("[Sistem] Data " + member.getNama() + " berhasil disimpan ke Supabase!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Gagal nyimpen ke Supabase!\n\nDetail Error: " + e.getMessage(),
                    "Error Database",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        members.add(member);
        for (Listener l : new ArrayList<>(listeners)) {
            l.onMemberAdded(member);
        }
    }

    public boolean updateMember(Member memberLama, String namaBaru, String noHpBaru, String tipeBaru) {
        Member memberBaru = buatObjekMember(memberLama, namaBaru, noHpBaru, tipeBaru);

        try {
            memberDAO.update(memberBaru);
            System.out.println("[Sistem] Data " + memberBaru.getIdMember() + " berhasil diperbarui di Supabase!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Gagal memperbarui data member di Supabase!\n\nDetail Error: " + e.getMessage(),
                    "Error Database",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int idx = members.indexOf(memberLama);
        if (idx >= 0) {
            members.set(idx, memberBaru);
        }

        for (Listener l : new ArrayList<>(listeners)) {
            l.onMemberUpdated();
        }
        return true;
    }

    public boolean nonaktifkanMember(String idMember) {
        try {
            memberDAO.nonaktifkan(idMember);
            System.out.println("[Sistem] Member " + idMember + " dinonaktifkan (Soft Delete) di Supabase!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Gagal menonaktifkan member di Supabase!\n\nDetail Error: " + e.getMessage(),
                    "Error Database",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        members.removeIf(m -> m.getIdMember().equals(idMember));
        for (Listener l : new ArrayList<>(listeners)) {
            l.onMemberUpdated();
        }
        return true;
    }

    private Member buatObjekMember(Member lama, String nama, String noHp, String tipeBaru) {
        LocalDate tglDaftar = lama.getTanggalDaftar();
        int durasi = lama.getDurasiBulan();

        if (tipeBaru != null && tipeBaru.toLowerCase().contains("vip")) {
            return new MemberVIP(lama.getIdMember(), nama, noHp, tglDaftar, durasi, "-");
        } else if (tipeBaru != null && tipeBaru.toLowerCase().contains("trainer")) {
            int sisaSesi = (lama instanceof PersonalTrainer) ? ((PersonalTrainer) lama).getSisaSesi() : 4;
            return new PersonalTrainer(lama.getIdMember(), nama, noHp, tglDaftar, durasi, "Rina Marlina", sisaSesi);
        } else {
            return new MemberRegular(lama.getIdMember(), nama, noHp, tglDaftar, durasi);
        }
    }

    public List<Member> cari(String kataKunci) {
        String k = (kataKunci == null) ? "" : kataKunci.trim().toLowerCase();
        List<Member> hasil = new ArrayList<>();
        for (Member m : members) {
            if (k.isEmpty()
                    || m.getIdMember().toLowerCase().contains(k)
                    || m.getNama().toLowerCase().contains(k)) {
                hasil.add(m);
            }
        }
        return hasil;
    }

    public List<Member> getSemuaMember() {
        return new ArrayList<>(members);
    }

    // ---------------------------------------------------------------
    // Presensi
    // ---------------------------------------------------------------
    public Presensi presensiHariIni(String idMember) {
        LocalDate hariIni = LocalDate.now();
        for (Presensi p : presensi) {
            if (p.getIdMember().equals(idMember) && p.getWaktuMasuk().toLocalDate().equals(hariIni)) {
                return p;
            }
        }
        return null;
    }

    public Presensi catatPresensi(Member member) {
        if (!member.isAktif() || presensiHariIni(member.getIdMember()) != null) {
            return null;
        }
        Presensi p = new Presensi(member, LocalDateTime.now());

        try {
            presensiDAO.insert(p);
            System.out.println("[Sistem] Check-in atas nama ID " + member.getIdMember() + " masuk Supabase!");

            if (member instanceof PersonalTrainer) {
                PersonalTrainer pt = (PersonalTrainer) member;
                int sisa = pt.getSisaSesi();
                if (sisa > 0) {
                    pt.setSisaSesi(sisa - 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Gagal nyimpen Check-in ke Database!\nError: " + e.getMessage(),
                    "Error Supabase",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        presensi.add(p);
        for (Listener l : new ArrayList<>(listeners)) {
            l.onCheckIn(p);
        }
        return p;
    }

    // ---------------------------------------------------------------
    // Jadwal Kelas
    // ---------------------------------------------------------------
    public void tambahJadwal(Jadwal jadwal) {
        try {
            jadwalDAO.insert(jadwal);
            System.out.println("[Sistem] Jadwal " + jadwal.getNamaKelas() + " berhasil disimpan ke Supabase!");
            daftarJadwal.add(jadwal);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Gagal nyimpen jadwal ke Supabase!\n\nDetail Error: " + e.getMessage(),
                    "Error Database",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public Jadwal cariBentrok(Jadwal baru) {
        for (Jadwal j : daftarJadwal) {
            if (j.bentrokDengan(baru)) {
                return j;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Trainer
    // ---------------------------------------------------------------
    public static class DataException extends Exception {
        public DataException(String message) {
            super(message);
        }
    }

    public void muatTrainerDariDB() {
        daftarTrainer.clear();
        try {
            daftarTrainer.addAll(trainerDAO.getAll());
            System.out.println("[Sistem] Berhasil menarik " + daftarTrainer.size() + " data trainer dari Supabase.");
        } catch (SQLException e) {
            System.err.println("[Error] Gagal narik data trainer: " + e.getMessage());
        }
    }

    public String generateIdTrainer() {
        int max = 0;
        for (String[] t : daftarTrainer) {
            try {
                max = Math.max(max, Integer.parseInt(t[0].substring(4)));
            } catch (RuntimeException ignored) {
            }
        }
        return String.format("TRN-%03d", max + 1);
    }

    public boolean adaNoHpTrainer(String noHp) {
        for (String[] t : daftarTrainer) {
            if (t[3] != null && t[3].equals(noHp)) {
                return true;
            }
        }
        return false;
    }

    public String[] tambahTrainer(String nama, String spesialisasi, String noHp, String status) throws DataException {
        long idBaru = daftarTrainer.size() + 1;

        try {
            long generatedId = trainerDAO.insert(nama, spesialisasi, noHp, status);
            if (generatedId > 0) {
                idBaru = generatedId;
            }
            System.out.println("[Sistem] Trainer " + nama + " berhasil disimpan ke Supabase!");
        } catch (SQLException e) {
            throw new DataException("Gagal menyimpan ke Supabase: " + e.getMessage());
        }

        String idFormat = String.format("TRN-%03d", idBaru);
        String[] barisBaru = new String[]{idFormat, nama, spesialisasi, noHp, status};
        daftarTrainer.add(barisBaru);
        return barisBaru;
    }

    public List<String[]> getSemuaTrainer() {
        return new ArrayList<>(daftarTrainer);
    }

    public String[] getDaftarTrainer() {
        List<String> namaAktif = new ArrayList<>();
        for (String[] t : daftarTrainer) {
            if ("Aktif".equalsIgnoreCase(t[4])) {
                String spes = (t[2] != null && !t[2].isEmpty()) ? " (" + t[2] + ")" : "";
                namaAktif.add(t[1] + spes);
            }
        }
        if (namaAktif.isEmpty()) {
            return new String[]{"Rina Marlina (Zumba & Aerobic)", "Ade Rai (Bodybuilding)"};
        }
        return namaAktif.toArray(new String[0]);
    }

    // ---------------------------------------------------------------
    // Transaksi
    // ---------------------------------------------------------------
    public void muatTransaksiDariDB() {
        daftarTransaksi.clear();
        try {
            daftarTransaksi.addAll(transaksiDAO.getAll(members));
            System.out.println("[Sistem] Berhasil menarik " + daftarTransaksi.size() + " data transaksi dari Supabase.");
        } catch (SQLException e) {
            System.err.println("[Error] Gagal narik data transaksi: " + e.getMessage());
        }
    }

    public String generateIdTransaksi() {
        int max = 0;
        for (Transaksi t : daftarTransaksi) {
            try {
                max = Math.max(max, Integer.parseInt(t.getIdTransaksi().substring(4)));
            } catch (RuntimeException ignored) {
            }
        }
        return String.format("TRX-%03d", max + 1);
    }
    
    public Transaksi buatTransaksi(Member member, int durasiBulan, int diskon, String metode, String status)
            throws DataException {
        if (member == null) {
            throw new DataException("Member tidak valid.");
        }

        long biaya = member.hitungBiayaPerpanjangan(durasiBulan);
        long total = Math.round(biaya * (100 - diskon) / 100.0);
        String idTrx = generateIdTransaksi();

        Transaksi trx = new Transaksi(
                idTrx,
                member,
                durasiBulan,
                Transaksi.PERPANJANGAN,
                total,
                metode,
                status,
                LocalDateTime.now()
        );

        try {
            transaksiDAO.insert(trx);
            System.out.println("[Sistem] Transaksi " + idTrx + " berhasil disimpan ke Supabase!");
        } catch (SQLException e) {
            throw new DataException("Gagal menyimpan transaksi ke Supabase: " + e.getMessage());
        }

        if (trx.isLunas()) {
            member.perpanjangPaket(durasiBulan);
            try {
                memberDAO.update(member);
            } catch (SQLException e) {
                System.err.println("[Error] Gagal update status member setelah transaksi: " + e.getMessage());
            }
        }

        daftarTransaksi.add(0, trx);
        for (Listener l : new ArrayList<>(listeners)) {
            l.onMemberUpdated();
        }
        return trx;
    }
    
    public boolean tambahTransaksi(Transaksi trx) {
        try {
            transaksiDAO.insert(trx);
            System.out.println("[Sistem] Transaksi " + trx.getIdTransaksi() + " berhasil disimpan ke Supabase!");
            daftarTransaksi.add(0, trx);

            if (trx.isLunas()) {
                for (Member m : members) {
                    if (m.getIdMember().equals(trx.getIdMember())) {
                        m.perpanjangPaket(trx.getDurasiBulan());
                        memberDAO.update(m);
                        break;
                    }
                }
                for (Listener l : new ArrayList<>(listeners)) {
                    l.onMemberUpdated();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Gagal nyimpen transaksi ke Supabase!\n\nDetail Error: " + e.getMessage(),
                    "Error Database",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public List<Transaksi> getSemuaTransaksi() {
        return new ArrayList<>(daftarTransaksi);
    }
    
    // ---------------------------------------------------------------
    // Data Grafik & Statistik Dashboard (untuk LineChartPanel & PieChartPanel)
    // ---------------------------------------------------------------
    public void addChangeListener(Runnable callback) {
        addListener(new Listener() {
            @Override public void onMemberAdded(Member member) { callback.run(); }
            @Override public void onMemberUpdated() { callback.run(); }
            @Override public void onCheckIn(Presensi presensi) { callback.run(); }
        });
    }

    public int[] getKunjunganBulanan() {
        int[] data = new int[12];
        for (Presensi p : presensi) {
            if (p.getWaktuMasuk() != null) {
                data[p.getWaktuMasuk().getMonthValue() - 1]++;
            }
        }
        return data;
    }

    public int[] getPendaftaranBulanan() {
        int[] data = new int[12];
        for (Member m : members) {
            if (m.getTanggalDaftar() != null) {
                data[m.getTanggalDaftar().getMonthValue() - 1]++;
            }
        }
        return data;
    }

    public int getCheckInRegulerHariIni() {
        LocalDate hariIni = LocalDate.now();
        int count = 0;
        for (Presensi p : presensi) {
            if (p.getWaktuMasuk() != null && p.getWaktuMasuk().toLocalDate().equals(hariIni)) {
                if (!isMemberVip(p.getIdMember())) count++;
            }
        }
        return count;
    }

    public int getCheckInVipHariIni() {
        LocalDate hariIni = LocalDate.now();
        int count = 0;
        for (Presensi p : presensi) {
            if (p.getWaktuMasuk() != null && p.getWaktuMasuk().toLocalDate().equals(hariIni)) {
                if (isMemberVip(p.getIdMember())) count++;
            }
        }
        return count;
    }

    private boolean isMemberVip(String idMember) {
        for (Member m : members) {
            if (m.getIdMember().equals(idMember)) {
                return (m instanceof MemberVIP) || (m instanceof PersonalTrainer);
            }
        }
        return false;
    }
}