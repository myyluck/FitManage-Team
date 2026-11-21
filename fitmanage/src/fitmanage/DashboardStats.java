package fitmanage;

import fitmanage.dao.KoneksiDB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.List;

/**
 * Pengumpul data statistik untuk Dashboard (KPI, chart tren, donut).
 * HANYA MEMBACA: memakai DataStore yang sudah ada + query SELECT lewat KoneksiDB
 * (pola yang sama dengan loadDataCheckInDariDB() di Dashboard lama).
 * DAO dan DataStore sama sekali tidak diubah.
 *
 * Catatan: kategori "Reguler" = semua member yang BUKAN VIP (termasuk Personal Trainer),
 * supaya total Reguler + VIP selalu sama dengan Total Member.
 */
public class DashboardStats {

    private static final String HIJAU = "#4ADE80";
    private static final String MERAH = "#F87171";

    public int totalMember;
    public int reguler;
    public int vip;

    public int checkInHariIni;
    public int checkInKemarin;
    public int regulerHadir;
    public int vipHadir;

    public int trainerAktif;
    public int trainerCuti;

    public long pendapatanBulanIni;
    public long pendapatanBulanLalu;

    public final int[] kunjunganBulanan = new int[12];
    public final int[] pendaftaranBulanan = new int[12];

    /** Menarik semua angka. Bila database gagal, angka terkait tetap 0 (aplikasi tidak crash). */
    public static DashboardStats load() {
        DashboardStats s = new DashboardStats();
        s.hitungDariDataStore();
        s.hitungDariDatabase();
        return s;
    }

    private void hitungDariDataStore() {
        DataStore store = DataStore.getInstance();

        List<Member> members = store.getSemuaMember();
        totalMember = members.size();
        for (Member m : members) {
            if ("VIP".equalsIgnoreCase(m.getJenisMember())) {
                vip++;
            } else {
                reguler++;
            }
        }

        for (String[] t : store.getSemuaTrainer()) {
            String status = t.length > 4 ? t[4] : null;
            if ("Aktif".equalsIgnoreCase(status)) {
                trainerAktif++;
            } else if ("Cuti".equalsIgnoreCase(status)) {
                trainerCuti++;
            }
        }

        YearMonth sekarang = YearMonth.now();
        YearMonth lalu = sekarang.minusMonths(1);
        for (Transaksi t : store.getSemuaTransaksi()) {
            if (!t.isLunas() || t.getWaktu() == null) {
                continue;
            }
            YearMonth ym = YearMonth.from(t.getWaktu());
            if (ym.equals(sekarang)) {
                pendapatanBulanIni += t.getTotal();
            } else if (ym.equals(lalu)) {
                pendapatanBulanLalu += t.getTotal();
            }
        }
    }

    private void hitungDariDatabase() {
        try (Connection conn = KoneksiDB.sambungDB()) {
            if (conn == null) {
                throw new SQLException("Koneksi database tidak tersedia.");
            }
            try (Statement stmt = conn.createStatement()) {

                // Check-in hari ini, dipisah per tipe member
                String qHariIni = "SELECT m.tipe_member, COUNT(*) AS jml "
                        + "FROM presensi p JOIN member m ON p.member_id = m.id "
                        + "WHERE DATE(p.created_at) = CURRENT_DATE GROUP BY m.tipe_member";
                try (ResultSet rs = stmt.executeQuery(qHariIni)) {
                    while (rs.next()) {
                        String tipe = rs.getString("tipe_member");
                        int jml = rs.getInt("jml");
                        checkInHariIni += jml;
                        if (tipe != null && tipe.toLowerCase().contains("vip")) {
                            vipHadir += jml;
                        } else {
                            regulerHadir += jml;
                        }
                    }
                }

                // Check-in kemarin (untuk teks perbandingan)
                String qKemarin = "SELECT COUNT(*) AS jml FROM presensi WHERE DATE(created_at) = CURRENT_DATE - 1";
                try (ResultSet rs = stmt.executeQuery(qKemarin)) {
                    if (rs.next()) {
                        checkInKemarin = rs.getInt("jml");
                    }
                }

                // Kunjungan per bulan (tahun berjalan)
                String qKunjungan = "SELECT EXTRACT(MONTH FROM created_at)::int AS bln, COUNT(*) AS jml "
                        + "FROM presensi WHERE EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE) "
                        + "GROUP BY 1";
                isiBulanan(stmt, qKunjungan, kunjunganBulanan);

                // Pendaftaran member per bulan (tahun berjalan)
                String qDaftar = "SELECT EXTRACT(MONTH FROM created_at)::int AS bln, COUNT(*) AS jml "
                        + "FROM member WHERE EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE) "
                        + "GROUP BY 1";
                isiBulanan(stmt, qDaftar, pendaftaranBulanan);
            }
        } catch (SQLException e) {
            System.err.println("[Sistem] Gagal memuat statistik dashboard: " + e.getMessage());
        }
    }

    private void isiBulanan(Statement stmt, String query, int[] target) throws SQLException {
        try (ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int bulan = rs.getInt("bln");
                if (bulan >= 1 && bulan <= 12) {
                    target[bulan - 1] = rs.getInt("jml");
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Teks keterangan untuk kartu KPI
    // ---------------------------------------------------------------
    public String subTotalMember() {
        return "Member Reguler: " + reguler + "   VIP: " + vip;
    }

    public String subCheckIn() {
        if (checkInKemarin == 0) {
            return checkInHariIni == 0 ? "Belum ada check-in kemarin" : "Kemarin belum ada check-in";
        }
        return banding(checkInHariIni, checkInKemarin, "kemarin");
    }

    public String subTrainer() {
        return trainerCuti > 0 ? trainerCuti + " trainer sedang cuti" : "Semua shift terisi";
    }

    public String subPendapatan() {
        if (pendapatanBulanLalu == 0) {
            return pendapatanBulanIni == 0 ? "Belum ada pendapatan bulan ini" : "Bulan lalu belum ada pendapatan";
        }
        return banding(pendapatanBulanIni, pendapatanBulanLalu, "bulan lalu");
    }

    public String pendapatanRupiah() {
        return Member.formatRupiah(pendapatanBulanIni);
    }

    private static String banding(long sekarang, long sebelum, String pembanding) {
        long persen = Math.round((sekarang - sebelum) * 100.0 / sebelum);
        if (persen > 0) {
            return "<html>Naik <font color='" + HIJAU + "'>" + persen + "%</font> dari " + pembanding + "</html>";
        } else if (persen < 0) {
            return "<html>Turun <font color='" + MERAH + "'>" + Math.abs(persen) + "%</font> dari " + pembanding + "</html>";
        }
        return "Sama seperti " + pembanding;
    }
}
