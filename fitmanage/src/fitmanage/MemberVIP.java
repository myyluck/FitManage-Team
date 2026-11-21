package fitmanage;

import java.time.LocalDate;

/** Anggota paket VIP: akses penuh semua area, kelas grup, dan loker pribadi. */
public class MemberVIP extends Member {

    public static final long HARGA_PER_BULAN = 200_000;

    private String nomorLoker;

    public MemberVIP(String idMember, String nama, String noHp, LocalDate tanggalDaftar,
                     int durasiBulan, String nomorLoker) {
        super(idMember, nama, noHp, tanggalDaftar, durasiBulan);
        this.nomorLoker = nomorLoker;
    }

    @Override
    public String getJenisMember() {
        return "VIP";
    }

    @Override
    public long getHargaPerBulan() {
        return HARGA_PER_BULAN;
    }

    @Override
    public String getBenefit() {
        return "Akses semua area, kelas grup tanpa batas, dan loker pribadi.";
    }

    @Override
    public String getInfoTambahan() {
        return "No. Loker: " + getNomorLoker();
    }

    public String getNomorLoker() {
        return (nomorLoker == null || nomorLoker.isEmpty()) ? "-" : nomorLoker;
    }

    public void setNomorLoker(String nomorLoker) {
        this.nomorLoker = nomorLoker;
    }
}