package fitmanage;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class Dashboard extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Dashboard.class.getName());

    public Dashboard() {
        initComponents();
        buildSidebar();
        buildCustomUI();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel(); // Main Background
        jPanel2 = new RoundedPanel(30);     // Sidebar Background

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(new java.awt.Dimension(1200, 700));
        setLocationRelativeTo(null); 

        // --- BACKGROUND UTAMA (PUTIH POLOS) ---
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(null); 

        // --- SIDEBAR (BIRU GELAP) ---
        jPanel2.setBackground(new java.awt.Color(10, 25, 60));
        jPanel2.setBounds(15, 15, 220, 670);
        jPanel2.setLayout(null);
        jPanel1.add(jPanel2);

        // --- HEADER TEXT ---
        JLabel lblWelcome = new JLabel("Welcome Back, MUKHLASH!");
        lblWelcome.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(new java.awt.Color(30, 30, 30));
        lblWelcome.setBounds(260, 30, 400, 35);
        jPanel1.add(lblWelcome);

        JLabel lblSubWelcome = new JLabel("Pantau dan kelola seluruh aktivitas gym Anda hari ini.");
        lblSubWelcome.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 12));
        lblSubWelcome.setForeground(new java.awt.Color(100, 100, 100));
        lblSubWelcome.setBounds(260, 65, 400, 20);
        jPanel1.add(lblSubWelcome);

        // --- TOMBOL AKSI CEPAT ---
        JButton btnTambah = new JButton("+ Tambah Member");
        btnTambah.setBackground(new java.awt.Color(0, 131, 176));
        btnTambah.setForeground(Color.WHITE);
        btnTambah.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 12));
        btnTambah.setBounds(630, 35, 150, 40);
        jPanel1.add(btnTambah);

        JButton btnCheckIn = new JButton("Check-In Member");
        btnCheckIn.setBackground(new java.awt.Color(230, 179, 51));
        btnCheckIn.setForeground(Color.WHITE);
        btnCheckIn.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 12));
        btnCheckIn.setBounds(795, 35, 150, 40);
        jPanel1.add(btnCheckIn);

        // --- PROFIL ADMIN (POJOK KANAN ATAS) ---
        RoundedPanel profileBox = new RoundedPanel(25);
        profileBox.setBackground(new java.awt.Color(245, 245, 245));
        profileBox.setBounds(960, 35, 200, 45);
        profileBox.setLayout(null);
        
        // Lingkaran Inisial M
        RoundedPanel circleM = new RoundedPanel(35);
        circleM.setBackground(new java.awt.Color(220, 53, 69)); // Merah/Pink tema
        circleM.setBounds(5, 5, 35, 35);
        circleM.setLayout(new BorderLayout());
        JLabel lblM = new JLabel("M", SwingConstants.CENTER);
        lblM.setForeground(Color.WHITE);
        lblM.setFont(new Font("Segoe UI", Font.BOLD, 16));
        circleM.add(lblM, BorderLayout.CENTER);
        profileBox.add(circleM);
        
        JLabel lblAdminName = new JLabel("Admin");
        lblAdminName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAdminName.setBounds(50, 8, 100, 15);
        profileBox.add(lblAdminName);
        
        JLabel lblAdminRole = new JLabel("Admin Utama");
        lblAdminRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblAdminRole.setForeground(Color.GRAY);
        lblAdminRole.setBounds(50, 23, 100, 15);
        profileBox.add(lblAdminRole);
        
        jPanel1.add(profileBox);

        // --- 4 KARTU METRIK DENGAN EFEK SHADOW & ICON ---
        JPanel card1 = createMetricCard("Total Member", "351", "Member Reguler: 251     Member VIP: 100", "👥");
        card1.setBounds(260, 110, 215, 105);
        jPanel1.add(card1);

        JPanel card2 = createMetricCard("Check-In Hari Ini", "184", "Naik 29% dari kemarin", "📅");
        card2.setBounds(495, 110, 215, 105);
        jPanel1.add(card2);

        JPanel card3 = createMetricCard("Trainer Aktif", "16", "Semua shift terisi", "🏋");
        card3.setBounds(730, 110, 215, 105);
        jPanel1.add(card3);

        JPanel card4 = createMetricCard("Pendapatan Bulan", "Rp 54.820.000", "Target bulan ini: Tercapai", "💰");
        card4.setBounds(965, 110, 215, 105);
        jPanel1.add(card4);

        // --- WADAH GRAFIK ---
        RoundedPanel panelGrafik = new RoundedPanel(20);
        panelGrafik.setBackground(new java.awt.Color(248, 248, 248));
        panelGrafik.setBounds(260, 235, 645, 210);
        panelGrafik.setLayout(null);
        JLabel titleGrafik1 = new JLabel("Tren Kunjungan & Pendaftaran (Bulanan)");
        titleGrafik1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleGrafik1.setBounds(20, 15, 250, 20);
        panelGrafik.add(titleGrafik1);
        jPanel1.add(panelGrafik);

        RoundedPanel panelPie = new RoundedPanel(20);
        panelPie.setBackground(new java.awt.Color(248, 248, 248));
        panelPie.setBounds(925, 235, 255, 210);
        panelPie.setLayout(null);
        JLabel titleGrafik2 = new JLabel("Member yg Datang Hari Ini");
        titleGrafik2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleGrafik2.setBounds(20, 15, 200, 20);
        panelPie.add(titleGrafik2);
        jPanel1.add(panelPie);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1200, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>                        

    // --- METHOD OOP: Membuat Sidebar Dinamis ---
    private void buildSidebar() {
        // Logo
        JLabel lblLogo = new JLabel("Fit Manage");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setBounds(60, 30, 150, 30);
        jPanel2.add(lblLogo);

        // Menu Navigasi
        String[] menus = {"Dashboard", "Data Member", "Kelas & Jadwal", "Manajemen Trainer", "Paket & Transaksi", "Laporan Analisis", "Pengaturan"};
        int yPos = 90;
        
        for (int i = 0; i < menus.length; i++) {
            if(i == 0) {
                // Menu Aktif (Dashboard) diblok warna biru muda
                RoundedPanel activeBox = new RoundedPanel(15);
                activeBox.setBackground(new Color(0, 131, 176, 50)); // Transparan dikit
                activeBox.setBounds(15, yPos - 5, 190, 35);
                jPanel2.add(activeBox);
            }
            JLabel menu = new JLabel(menus[i]);
            menu.setFont(new Font("Segoe UI", i==0 ? Font.BOLD : Font.PLAIN, 13));
            menu.setForeground(Color.WHITE);
            menu.setBounds(50, yPos, 150, 25);
            jPanel2.add(menu);
            yPos += 45;
        }

        // Tombol Logout di bawah
        JLabel lblLogout = new JLabel("Logout");
        lblLogout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLogout.setForeground(new Color(200, 200, 200));
        lblLogout.setBounds(50, 610, 100, 25);
        jPanel2.add(lblLogout);
    }

    // --- HELPER METHOD: Membuat Kartu Metrik dengan Shadow & Icon ---
    private JPanel createMetricCard(String title, String value, String subText, String iconSymbol) {
        // Wrapper Panel (Transparan) untuk menumpuk card asli & bayangannya
        JPanel wrapper = new JPanel(null);
        wrapper.setOpaque(false);

        // 1. Kotak Utama (Putih/Abu Muda)
        RoundedPanel mainCard = new RoundedPanel(20);
        mainCard.setBackground(new java.awt.Color(250, 250, 250)); 
        mainCard.setBounds(0, 0, 210, 100);
        mainCard.setLayout(null);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new java.awt.Color(80, 80, 80));
        lblTitle.setBounds(15, 10, 150, 20);
        mainCard.add(lblTitle);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(new java.awt.Color(20, 20, 20));
        lblValue.setBounds(15, 30, 180, 40);
        mainCard.add(lblValue);

        JLabel lblSub = new JLabel(subText);
        lblSub.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new java.awt.Color(120, 120, 120));
        lblSub.setBounds(15, 75, 180, 15);
        mainCard.add(lblSub);

        // Label Ikon di Pojok Kanan Atas Kotak (Bisa di-setIcon nanti)
        JLabel lblIcon = new JLabel(iconSymbol, SwingConstants.RIGHT);
        lblIcon.setFont(new java.awt.Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblIcon.setBounds(170, 10, 30, 30);
        mainCard.add(lblIcon);

        // Garis batas halus di bagian bawah
        JPanel bottomBorder = new JPanel();
        bottomBorder.setBackground(new java.awt.Color(220, 220, 220));
        bottomBorder.setBounds(15, 95, 180, 1);
        mainCard.add(bottomBorder);

        // 2. Kotak Shadow (Abu-abu Gelap) sedikit melenceng ke kanan bawah
        RoundedPanel shadow = new RoundedPanel(20);
        shadow.setBackground(new java.awt.Color(230, 230, 230)); // Warna bayangan
        shadow.setBounds(3, 4, 210, 100); // Offset x+3, y+4

        // Susun ke Wrapper (Yang pertama di-add posisinya ada di ATAS)
        wrapper.add(mainCard);
        wrapper.add(shadow);

        return wrapper;
    }

    // --- HELPER METHOD: Membangun Tabel Data Check-In ---
    private void buildCustomUI() {
        JLabel lblTabel = new JLabel("Check-In Terbaru");
        lblTabel.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 14));
        lblTabel.setBounds(260, 460, 200, 20);
        jPanel1.add(lblTabel);

        // Kotak Wrapper untuk Tabel
        RoundedPanel tableContainer = new RoundedPanel(20);
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBounds(260, 490, 920, 180);
        tableContainer.setLayout(new BorderLayout());

        String[] kolom = {"No", "Nama", "ID Member", "Tipe Member", "Waktu Check-In", "Status"};
        Object[][] dataDummy = {
            {"1", "Budi Santoso", "000030827", "Reguler", "05/11/2024 13:00", "Aktif"},
            {"2", "Andi Wijaya", "000030734", "VIP", "05/11/2024 13:00", "Hampir Habis"},
            {"3", "Rina Marlina", "000040521", "Reguler", "05/11/2024 13:00", "Aktif"},
            {"4", "Joko Susilo", "000040588", "Reguler", "05/11/2024 14:15", "Aktif"}
        };

        DefaultTableModel model = new DefaultTableModel(dataDummy, kolom);
        JTable tblCheckIn = new JTable(model);
        tblCheckIn.setFont(new java.awt.Font("Segoe UI", Font.PLAIN, 12));
        tblCheckIn.setRowHeight(35);
        tblCheckIn.setShowGrid(false); // Hilangkan garis jaring
        
        // Kustomisasi Header Tabel (Warna Biru Khas)
        JTableHeader header = tblCheckIn.getTableHeader();
        header.setBackground(new Color(10, 25, 60)); // Warna biru gelap persis sidebar
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(100, 40));
        header.setOpaque(true);

        JScrollPane scrollPane = new JScrollPane(tblCheckIn);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margin dalam
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        jPanel1.add(tableContainer);
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("FlatLaf failed to initialize");
        }
        java.awt.EventQueue.invokeLater(() -> new Dashboard().setVisible(true));
    }

    // Variables declaration
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration
}
