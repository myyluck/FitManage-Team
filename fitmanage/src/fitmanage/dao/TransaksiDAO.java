package fitmanage.dao;

import fitmanage.Member;
import fitmanage.Transaksi;
import java.sql.SQLException;
import java.util.List;

public interface TransaksiDAO {
    List<Transaksi> getAll(List<Member> daftarMember) throws SQLException;
    void insert(Transaksi transaksi) throws SQLException;
}