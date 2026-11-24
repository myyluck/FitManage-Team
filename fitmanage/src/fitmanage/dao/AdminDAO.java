package fitmanage.dao;

import java.sql.SQLException;

public interface AdminDAO {
    boolean cekLogin(String username, String password) throws SQLException;
}