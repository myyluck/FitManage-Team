package fitmanage.dao;

import fitmanage.Member;
import fitmanage.MemberRegular;
import fitmanage.MemberVIP;
import fitmanage.PersonalTrainer;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberDAOImpl implements MemberDAO {

    @Override
    public List<Member> getAll() throws SQLException {
        List<Member> list = new ArrayList<>();
        String query = "SELECT m.id, m.nama, m.no_telepon, m.tipe_member, m.status, m.created_at, "
                     + "(SELECT COUNT(*) FROM presensi p WHERE p.member_id = m.id) AS sesi_terpakai "
                     + "FROM member m "
                     + "WHERE COALESCE(m.status, '') != 'Nonaktif' "
                     + "ORDER BY m.id ASC";

        try (Connection conn = KoneksiDB.sambungDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String nama = rs.getString("nama");
                String noHp = rs.getString("no_telepon");
                String tipe = rs.getString("tipe_member");

                Timestamp tStamp = rs.getTimestamp("created_at");
                LocalDate tglDaftarAsli = (tStamp != null) ? tStamp.toLocalDateTime().toLocalDate() : LocalDate.now();

                int sesiTerpakai = rs.getInt("sesi_terpakai");
                int sisaSesi = Math.max(0, 4 - sesiTerpakai);

                if (tipe != null && tipe.toLowerCase().contains("vip")) {
                    list.add(new MemberVIP(id, nama, noHp, tglDaftarAsli, 1, "-"));
                } else if (tipe != null && tipe.toLowerCase().contains("trainer")) {
                    list.add(new PersonalTrainer(id, nama, noHp, tglDaftarAsli, 1, "Rina Marlina", sisaSesi));
                } else {
                    list.add(new MemberRegular(id, nama, noHp, tglDaftarAsli, 1));
                }
            }
        }
        return list;
    }

    @Override
    public void insert(Member member) throws SQLException {
        String query = "INSERT INTO member (id, nama, no_telepon, tipe_member, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = KoneksiDB.sambungDB();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, member.getIdMember());
            pstmt.setString(2, member.getNama());
            pstmt.setString(3, member.getNoHp());
            pstmt.setString(4, member.getJenisMember());
            pstmt.setString(5, member.getStatus().getLabel());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void update(Member member) throws SQLException {
        String query = "UPDATE member SET nama = ?, no_telepon = ?, tipe_member = ?, status = ? WHERE id = ?";

        try (Connection conn = KoneksiDB.sambungDB();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, member.getNama());
            pstmt.setString(2, member.getNoHp());
            pstmt.setString(3, member.getJenisMember());
            pstmt.setString(4, member.getStatus().getLabel());
            pstmt.setString(5, member.getIdMember());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void nonaktifkan(String idMember) throws SQLException {
        String query = "UPDATE member SET status = 'Nonaktif' WHERE id = ?";

        try (Connection conn = KoneksiDB.sambungDB();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, idMember);
            pstmt.executeUpdate();
        }
    }
}