package fitmanage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Kelas induk (abstract) untuk semua jenis anggota gym.
 *
 * Konsep PBO:
 *  - Abstraction   : class abstract dengan method abstract (getJenisMember, getHargaPerBulan, getBenefit)
 *  - Encapsulation : atribut private, diakses lewat getter/setter
 *  - Inheritance   : diturunkan menjadi MemberRegular, MemberVIP, dan PersonalTrainer
 *  - Polymorphism  : tiap turunan mengisi/meng-override method sesuai jenisnya
 */
public abstract class Member {

    /** Status paket langganan, dihitung otomatis dari tanggal berakhir. */
    public enum Status {
        AKTIF("Aktif"),
        HAMPIR_HABIS("Hampir Habis"),
        KEDALUWARSA("Kedaluwarsa");

        private final String label;

        Status(String label) { this.label = label; }

        public String getLabel() { return label; }

        @Override
        public String toString() { return label; }
    }

    /** Paket dianggap "Hampir Habis" jika sisa hari <= batas ini. */
    public static final int BATAS_HAMPIR_HABIS_HARI = 7;

    private final String idMember;
    private String nama;
    private String noHp;
    private final LocalDate tanggalDaftar;
    private int durasiBulan;
    private LocalDate tanggalBerakhir;

    protected Member(String idMember, String nama, String noHp, LocalDate tanggalDaftar, int durasiBulan) {
        this.idMember = idMember;
        this.nama = nama;
        this.noHp = noHp;
        this.tanggalDaftar = tanggalDaftar;
        this.durasiBulan = durasiBulan;
        this.tanggalBerakhir = tanggalDaftar.plusMonths(durasiBulan);
    }

    // ---------------------------------------------------------------
    // Method abstract: WAJIB diisi oleh setiap kelas turunan
    // ---------------------------------------------------------------

    /** Nama jenis member yang ditampilkan di tabel/form (mis. "Reguler"). */
    public abstract String getJenisMember();

    /** Harga paket per bulan (Rupiah). */
    public abstract long getHargaPerBulan();

    /** Ringkasan fasilitas yang didapat member. */
    public abstract String getBenefit();

    // ---------------------------------------------------------------
    // Method umum (boleh di-override oleh turunan)
    // ---------------------------------------------------------------

    /** Total biaya paket. PersonalTrainer meng-override ini karena ada biaya sesi. */
    public long hitungTotalBayar() {
        return getHargaPerBulan() * durasiBulan;
    }
    
    /** Hitung biaya perpanjangan paket sesuai durasi bulan yang dipilih. */
    public long hitungBiayaPerpanjangan(int bulan) {
        return getHargaPerBulan() * bulan;
    }

    /** Informasi tambahan khusus jenis member (loker, sisa sesi, dst). Default: kosong. */
    public String getInfoTambahan() {
        return "";
    }

    /** Sisa hari paket (negatif jika sudah lewat). */
    public long getSisaHari() {
        return ChronoUnit.DAYS.between(LocalDate.now(), tanggalBerakhir);
    }

    /** Paket masih berlaku (boleh check-in) selama belum melewati tanggal berakhir. */
    public boolean isAktif() {
        return getSisaHari() >= 0;
    }

    public Status getStatus() {
        long sisa = getSisaHari();
        if (sisa < 0) return Status.KEDALUWARSA;
        if (sisa <= BATAS_HAMPIR_HABIS_HARI) return Status.HAMPIR_HABIS;
        return Status.AKTIF;
    }

    /**
     * Sinkronisasi paket langganan: perpanjang paket.
     * Jika paket masih aktif, masa berlaku disambung dari tanggal berakhir;
     * jika sudah kedaluwarsa, dihitung mulai hari ini.
     */
   public void perpanjangPaket(int bulan) {
        perpanjangPaket(bulan, LocalDate.now());
    }

    public void perpanjangPaket(int bulan, LocalDate tanggalTransaksi) {
        LocalDate acuan = (tanggalTransaksi != null) ? tanggalTransaksi : LocalDate.now();
        LocalDate mulai = !acuan.isAfter(tanggalBerakhir) ? tanggalBerakhir : acuan;
        tanggalBerakhir = mulai.plusMonths(bulan);
        durasiBulan += bulan;
    }

    /** Format angka ke Rupiah, contoh: 1200000 -> "Rp 1.200.000". */
    public static String formatRupiah(long nilai) {
        return "Rp " + String.format(Locale.US, "%,d", nilai).replace(',', '.');
    }

    // ---------------------------------------------------------------
    // Getter & setter (encapsulation)
    // ---------------------------------------------------------------

    public String getIdMember()            { return idMember; }
    public String getNama()                { return nama; }
    public void setNama(String nama)       { this.nama = nama; }
    public String getNoHp()                { return noHp; }
    public void setNoHp(String noHp)       { this.noHp = noHp; }
    public LocalDate getTanggalDaftar()    { return tanggalDaftar; }
    public int getDurasiBulan()            { return durasiBulan; }
    public LocalDate getTanggalBerakhir()  { return tanggalBerakhir; }

    @Override
    public String toString() {
        return idMember + " - " + nama + " (" + getJenisMember() + ")";
    }
}