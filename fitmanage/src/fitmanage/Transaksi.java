package fitmanage;

import java.time.LocalDateTime;

/** Satu transaksi pembayaran paket (pendaftaran baru atau perpanjangan). */
public class Transaksi {

    public static final String PENDAFTARAN = "Pendaftaran";
    public static final String PERPANJANGAN = "Perpanjangan";
    public static final String[] METODE = {"Tunai", "Transfer Bank", "QRIS", "Kartu Debit"};

    private final String idTransaksi;
    private final String idMember;
    private final String namaMember;
    private final String jenisMember;
    private final int durasiBulan;
    private final String jenis;        // PENDAFTARAN / PERPANJANGAN
    private final long total;
    private final String metode;
    private final String status;       // "Lunas" / "Pending"
    private final LocalDateTime waktu;

    public Transaksi(String idTransaksi, Member member, int durasiBulan, String jenis,
                     long total, String metode, String status, LocalDateTime waktu) {
        this.idTransaksi = idTransaksi;
        this.idMember = member.getIdMember();
        this.namaMember = member.getNama();
        this.jenisMember = member.getJenisMember();
        this.durasiBulan = durasiBulan;
        this.jenis = jenis;
        this.total = total;
        this.metode = metode;
        this.status = status;
        this.waktu = waktu;
    }

    public String getIdTransaksi()  { return idTransaksi; }
    public String getIdMember()     { return idMember; }
    public String getNamaMember()   { return namaMember; }
    public int getDurasiBulan()     { return durasiBulan; }
    public String getJenis()        { return jenis; }
    public long getTotal()          { return total; }
    public String getMetode()       { return metode; }
    public String getStatus()       { return status; }
    public LocalDateTime getWaktu() { return waktu; }
    public boolean isLunas()        { return "Lunas".equals(status); }

    /** Contoh: "Reguler 3 Bulan" atau "VIP 1 Bulan (Perpanjangan)". */
    public String getPaketTeks() {
        String teks = jenisMember + " " + durasiBulan + " Bulan";
        return PERPANJANGAN.equals(jenis) ? teks + " (Perpanjangan)" : teks;
    }
}