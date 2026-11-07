package fitmanage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KoneksiDB {
    
    private static Connection koneksi;
    
    public static Connection sambungDB() {
        try {
            if (koneksi == null || koneksi.isClosed()) {
                
                // 1. Konfigurasi Kredensial Database
                String url = "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres";
                String user = "postgres.kupuqkzcafkxgcruzgcd";
                
                // password database
                String password = "u5lA8FUqAOrsiHq3"; 
                
                // 2. Inisialisasi Koneksi
                koneksi = DriverManager.getConnection(url, user, password);
                System.out.println("[Sistem] Koneksi ke database Supabase berhasil diinisialisasi.");
            }
        } catch (SQLException e) {
            System.err.println("[Sistem] Gagal terhubung ke database. Detail: " + e.getMessage());
        }
        
        return koneksi;
    }
    public static void main(String[] args) {
        sambungDB();
    }
}