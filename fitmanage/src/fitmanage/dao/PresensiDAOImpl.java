package fitmanage.dao;

import fitmanage.Member;
import fitmanage.Presensi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresensiDAOImpl implements PresensiDAO {

    @Override
    public List<Presensi> getAll(List<Member> daftarMember) throws SQLException {
        String query = "SELECT member_id, created_at FROM presensi ORDER BY created_at ASC";
        return ambilDataPresensi(query, daftarMember);
    }

    @Override
    public List<Presensi> getHariIni(List<Member> daftarMember) throws SQLException {
        String query = "SELECT member_id, created_at FROM presensi WHERE DATE(created_at) = CURRENT_DATE";
        return ambilDataPresensi(query, daftarMember);
    }

    private List<Presensi> ambilDataPresensi(String query, List<Member> daftarMember) throws SQLException {
        List<Presensi> list = new ArrayList<>();

        try (Connection conn = KoneksiDB.sambungDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String memberId = rs.getString("member_id");
                Timestamp tStamp = rs.getTimestamp("created_at");

                Member targetMember = null;
                for (Member m : daftarMember) {
                    if (m.getIdMember().equals(memberId)) {
                        targetMember = m;
                        break;
                    }
                }

                if (targetMember != null && tStamp != null) {
                    list.add(new Presensi(targetMember, tStamp.toLocalDateTime()));
                }
            }
        }
        return list;
    }

    @Override
    public void insert(Presensi presensi) throws SQLException {
        String query = "INSERT INTO presensi (member_id, created_at) VALUES (?, ?)";

        try (Connection conn = KoneksiDB.sambungDB();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, presensi.getIdMember());
            pstmt.setTimestamp(2, Timestamp.valueOf(presensi.getWaktuMasuk()));
            pstmt.executeUpdate();
        }
    }
}