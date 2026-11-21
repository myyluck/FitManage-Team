package fitmanage;

import java.time.LocalDate;

/**
 * Anggota paket Personal Trainer: akses gym + jatah sesi latihan privat
 * bersama seorang trainer.
 */
public class PersonalTrainer extends Member {

    public static final long HARGA_PER_BULAN = 250_000;
    public static final long HARGA_PER_SESI  = 75_000;

    // --- ATRIBUT KELAS WAJIB ADA DI ATAS ---
    private String namaTrainer;
    private final int jumlahSesi;
    private int sisaSesi;

    public PersonalTrainer(String idMember, String nama, String noHp, LocalDate tanggalDaftar,
                           int durasiBulan, String namaTrainer, int jumlahSesi) {
        super(idMember, nama, noHp, tanggalDaftar, durasiBulan);
        this.namaTrainer = namaTrainer;
        this.jumlahSesi = jumlahSesi;
        this.sisaSesi = jumlahSesi;
    }

    @Override
    public String getJenisMember() {
        return "Personal Trainer";
    }

    @Override
    public long getHargaPerBulan() {
        return HARGA_PER_BULAN;
    }

    @Override
    public long hitungTotalBayar() {
        return super.hitungTotalBayar() + HARGA_PER_SESI * jumlahSesi;
    }

    @Override
    public String getBenefit() {
        return "Akses gym + " + jumlahSesi + " sesi latihan privat bersama trainer.";
    }

    @Override
    public String getInfoTambahan() {
        return "Trainer: " + namaTrainer + "  |  Sisa sesi: " + sisaSesi + "/" + jumlahSesi;
    }

    public boolean reservasiSesi() {
        if (!isAktif() || sisaSesi <= 0) return false;
        sisaSesi--;
        return true;
    }

    public String getNamaTrainer()              { return namaTrainer; }
    public void setNamaTrainer(String nama)     { this.namaTrainer = nama; }
    public int getJumlahSesi()                  { return jumlahSesi; }
    public int getSisaSesi()                    { return sisaSesi; }
    public void setSisaSesi(int sisaSesi)       { this.sisaSesi = sisaSesi; }
}