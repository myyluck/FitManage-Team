package fitmanage;

import java.time.LocalTime;

/** Satu jadwal kelas kebugaran (mis. Zumba Dance, Senin 16:00 - 17:00). */
public class Jadwal {

    public static final String[] HARI = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};

    private final String namaKelas;
    private final String instruktur;
    private final String hari;
    private final LocalTime mulai;
    private final int durasiMenit;
    private final int kapasitas;
    private int peserta;

    public Jadwal(String namaKelas, String instruktur, String hari, LocalTime mulai,
                  int durasiMenit, int kapasitas, int peserta) {
        this.namaKelas = namaKelas;
        this.instruktur = instruktur;
        this.hari = hari;
        this.mulai = mulai;
        this.durasiMenit = durasiMenit;
        this.kapasitas = kapasitas;
        this.peserta = peserta;
    }

    public String getNamaKelas()   { return namaKelas; }
    public String getInstruktur()  { return instruktur; }
    public String getHari()        { return hari; }
    public LocalTime getMulai()    { return mulai; }
    public LocalTime getSelesai()  { return mulai.plusMinutes(durasiMenit); }
    public int getDurasiMenit()    { return durasiMenit; }
    public int getKapasitas()      { return kapasitas; }
    public int getPeserta()        { return peserta; }

    /** Contoh: "Senin, 16:00 - 17:00". */
    public String getHariJamTeks() {
        return hari + ", " + mulai + " - " + getSelesai();
    }

    /** Contoh: "15/20 Orang". */
    public String getKapasitasTeks() {
        return peserta + "/" + kapasitas + " Orang";
    }

    /** "Penuh" jika peserta sudah mencapai kapasitas, selain itu "Tersedia". */
    public String getStatus() {
        return peserta >= kapasitas ? "Penuh" : "Tersedia";
    }

    /** Dua jadwal bentrok jika instrukturnya sama, harinya sama, dan jamnya saling tumpang tindih. */
    public boolean bentrokDengan(Jadwal lain) {
        return instruktur.equals(lain.instruktur)
                && hari.equals(lain.hari)
                && mulai.isBefore(lain.getSelesai())
                && lain.mulai.isBefore(getSelesai());
    }
}