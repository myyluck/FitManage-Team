package fitmanage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAOImpl implements AdminDAO {

    @Override
    public boolean cekLogin(String username, String password) throws SQLException {
        String query = "SELECT * FROM admin WHERE username = ? AND password = ?";

        try (Connection conn = KoneksiDB.sambungDB();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[Sistem] Login berhasil untuk user: " + username);
                    return true;
                } else {
                    System.out.println("[Sistem] Login gagal. Kombinasi salah.");
                    return false;
                }
            }
        }
    }
}