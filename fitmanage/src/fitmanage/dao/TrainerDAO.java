package fitmanage.dao;

import java.sql.SQLException;
import java.util.List;

public interface TrainerDAO {
    List<String[]> getAll() throws SQLException;
    long insert(String nama, String spesialisasi, String noHp, String status) throws SQLException;
}