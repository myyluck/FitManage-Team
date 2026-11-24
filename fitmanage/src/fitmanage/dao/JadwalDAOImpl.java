package fitmanage.dao;

import fitmanage.Jadwal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;

public class JadwalDAOImpl implements JadwalDAO {

    @Override
    public void insert(Jadwal jadwal) throws SQLException {
        String query = "INSERT INTO jadwal_kelas (nama_kelas, instruktur, hari, jam_mulai, durasi_menit, kapasitas, peserta) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = KoneksiDB.sambungDB();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, jadwal.getNamaKelas());
            pstmt.setString(2, jadwal.getInstruktur());
            pstmt.setString(3, jadwal.getHari());
            pstmt.setTime(4, Time.valueOf(jadwal.getMulai()));
            pstmt.setInt(5, jadwal.getDurasiMenit());
            pstmt.setInt(6, jadwal.getKapasitas());
            pstmt.setInt(7, jadwal.getPeserta());

            pstmt.executeUpdate();
        }
    }
}