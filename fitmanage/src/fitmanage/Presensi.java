package fitmanage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Satu catatan kehadiran (check-in) member. */
public class Presensi {

    private static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String idMember;
    private final String nama;
    private final String jenisMember;
    private final String statusMember;
    private final LocalDateTime waktuMasuk;

    public Presensi(Member member, LocalDateTime waktuMasuk) {
        this.idMember = member.getIdMember();
        this.nama = member.getNama();
        this.jenisMember = member.getJenisMember();
        this.statusMember = member.getStatus().getLabel();
        this.waktuMasuk = waktuMasuk;
    }

    public String getIdMember()          { return idMember; }
    public String getNama()              { return nama; }
    public String getJenisMember()       { return jenisMember; }
    public String getStatusMember()      { return statusMember; }
    public LocalDateTime getWaktuMasuk() { return waktuMasuk; }

    /** Contoh: "05/11/2024 13:00" (sama dengan format di tabel). */
    public String getWaktuFormatted() {
        return waktuMasuk.format(FORMAT_WAKTU);
    }
}