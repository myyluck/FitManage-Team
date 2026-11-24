package fitmanage.dao;

import fitmanage.Member;
import fitmanage.Transaksi;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDAOImpl implements TransaksiDAO {

    @Override
    public List<Transaksi> getAll(List<Member> daftarMember) throws SQLException {
        List<Transaksi> list = new ArrayList<>();
        String query = "SELECT id, member_id, durasi_bulan, jenis, total, metode, status, created_at "
                     + "FROM transaksi ORDER BY created_at DESC";

        try (Connection conn = KoneksiDB.sambungDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String memberId = rs.getString("member_id");
                int durasi = rs.getInt("durasi_bulan");
                String jenis = rs.getString("jenis");
                long total = rs.getLong("total");
                String metode = rs.getString("metode");
                String status = rs.getString("status");
                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime waktu = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

                Member targetMember = null;
                for (Member m : daftarMember) {
                    if (m.getIdMember().equals(memberId)) {
                        targetMember = m;
                        break;
                    }
                }

                if (targetMember != null) {
                    list.add(new Transaksi(id, targetMember, durasi, jenis, total, metode, status, waktu));
                }
            }
        }
        return list;
    }

    @Override
    public void insert(Transaksi transaksi) throws SQLException {
        String query = "INSERT INTO transaksi (id, member_id, durasi_bulan, jenis, total, metode, status, created_at) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = KoneksiDB.sambungDB();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, transaksi.getIdTransaksi());
            pstmt.setString(2, transaksi.getIdMember());
            pstmt.setInt(3, transaksi.getDurasiBulan());
            pstmt.setString(4, transaksi.getJenis());
            pstmt.setLong(5, transaksi.getTotal());
            pstmt.setString(6, transaksi.getMetode());
            pstmt.setString(7, transaksi.getStatus());
            pstmt.setTimestamp(8, Timestamp.valueOf(transaksi.getWaktu()));
            pstmt.executeUpdate();
        }
    }
}