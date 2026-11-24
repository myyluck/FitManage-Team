package fitmanage.dao;

import fitmanage.Jadwal;
import java.sql.SQLException;

public interface JadwalDAO {
    void insert(Jadwal jadwal) throws SQLException;
}