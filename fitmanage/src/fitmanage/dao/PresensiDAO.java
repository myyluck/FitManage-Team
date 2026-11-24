package fitmanage.dao;

import fitmanage.Member;
import fitmanage.Presensi;
import java.sql.SQLException;
import java.util.List;

public interface PresensiDAO {
    List<Presensi> getAll(List<Member> daftarMember) throws SQLException;
    List<Presensi> getHariIni(List<Member> daftarMember) throws SQLException;
    void insert(Presensi presensi) throws SQLException;
}