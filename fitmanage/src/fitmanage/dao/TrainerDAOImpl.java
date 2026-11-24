package fitmanage.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrainerDAOImpl implements TrainerDAO {

    @Override
    public List<String[]> getAll() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT id, nama, no_telepon, spesialisasi, status_tersedia FROM trainer ORDER BY id ASC";

        try (Connection conn = KoneksiDB.sambungDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                long idAsli = rs.getLong("id");
                String idFormat = String.format("TRN-%03d", idAsli);
                String nama = rs.getString("nama");
                String noHp = rs.getString("no_telepon");
                String spes = rs.getString("spesialisasi");
                String status = rs.getString("status_tersedia");

                list.add(new String[]{idFormat, nama, spes, noHp, status});
            }
        }
        return list;
    }

    @Override
    public long insert(String nama, String spesialisasi, String noHp, String status) throws SQLException {
        String query = "INSERT INTO trainer (nama, no_telepon, spesialisasi, status_tersedia) VALUES (?, ?, ?, ?)";
        long idBaru = -1;

        try (Connection conn = KoneksiDB.sambungDB();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, nama);
            pstmt.setString(2, noHp);
            pstmt.setString(3, spesialisasi);
            pstmt.setString(4, status);
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    idBaru = keys.getLong(1);
                }
            }
        }
        return idBaru;
    }
}