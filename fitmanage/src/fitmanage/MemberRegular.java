package fitmanage;

import java.time.LocalDate;

/** Anggota paket Reguler: akses area gym standar. */
public class MemberRegular extends Member {

    public static final long HARGA_PER_BULAN = 150_000;

    public MemberRegular(String idMember, String nama, String noHp, LocalDate tanggalDaftar, int durasiBulan) {
        super(idMember, nama, noHp, tanggalDaftar, durasiBulan);
    }

    @Override
    public String getJenisMember() {
        return "Reguler";
    }

    @Override
    public long getHargaPerBulan() {
        return HARGA_PER_BULAN;
    }

    @Override
    public String getBenefit() {
        return "Akses area gym reguler setiap hari operasional.";
    }
}