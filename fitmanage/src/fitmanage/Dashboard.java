package fitmanage;

import fitmanage.dao.KoneksiDB;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.awt.Component;
import java.awt.Container;

public class Dashboard extends javax.swing.JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private ArrayList<JPanel> menuPanels = new ArrayList<>(); 
    private int activeMenuIndex = 0; 

    // DAFTAR MENU FINAL
    private final String[] menus = {
        "Dashboard", 
        "Data Member", 
        "Kelas & Jadwal", 
        "Manajemen Trainer", 
        "Manajemen Paket & Transaksi", 
        "Riwayat Presensi"
    };

    public Dashboard() {
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Fit Manage - Sistem Manajemen Gym");
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel rootPanel = new JPanel(new BorderLayout(20, 0));
        rootPanel.setBackground(new Color(255, 255, 255));
        rootPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(rootPanel);

        // ==========================================
        // 1. KONTEN UTAMA (CardLayout)
        // ==========================================
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // Mendaftarkan 6 Halaman ke CardLayout
        contentPanel.add(createDashboardPanel(), menus[0]);
        contentPanel.add(createDataMemberPanel(), menus[1]); 
        contentPanel.add(createKelasJadwalPanel(), menus[2]);
        contentPanel.add(createManajemenTrainerPanel(), menus[3]);
        contentPanel.add(createManajemenPaketPanel(), menus[4]);
        contentPanel.add(createRiwayatPresensiPanel(), menus[5]);
        
        rootPanel.add(contentPanel, BorderLayout.CENTER);

        // ==========================================
        // 2. SIDEBAR (Kiri)
        // ==========================================
        buildSidebar(rootPanel);

        // FITUR BARU DARI AJI: sambungkan form Tambah Member & Check-In ke tombol dan tabel
        wireFeatures();
    }

    // =========================================================================
    // HALAMAN 1: DASHBOARD UTAMA
    // =========================================================================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);
        JLabel lblWelcome = new JLabel("Welcome Back!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblSubWelcome = new JLabel("Pantau dan kelola seluruh aktivitas gym Anda hari ini.");
        lblSubWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubWelcome.setForeground(new Color(100, 100, 100));
        welcomePanel.add(lblWelcome);
        welcomePanel.add(lblSubWelcome);
        headerPanel.add(welcomePanel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);
        
        // --- Action Listener Check-In ---
        JButton btnCheckIn = new JButton("Check-In Hari Ini");
        btnCheckIn.setBackground(new Color(230, 179, 51));
        btnCheckIn.setForeground(Color.WHITE);
        btnCheckIn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCheckIn.setPreferredSize(new Dimension(150, 45));
        btnCheckIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCheckIn.addActionListener(e -> openCheckInDialog()); 

        // ---Action Listener Tambah Member ---
        JButton btnTambahMember = new JButton("+ Tambah Member");
        btnTambahMember.setBackground(new Color(0, 131, 176));
        btnTambahMember.setForeground(Color.WHITE);
        btnTambahMember.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTambahMember.setPreferredSize(new Dimension(160, 45));
        btnTambahMember.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTambahMember.addActionListener(e -> openAddMemberDialog()); 
        
        RoundedPanel profileBox = new RoundedPanel(25, new Color(245, 245, 245));
        profileBox.setPreferredSize(new Dimension(180, 45));
        profileBox.setLayout(new BorderLayout(10, 0));
        profileBox.setBorder(new EmptyBorder(5, 5, 5, 10));
        RoundedPanel circleM = new RoundedPanel(35, new Color(220, 53, 69));
        circleM.setPreferredSize(new Dimension(35, 35));
        circleM.setLayout(new BorderLayout());
        JLabel lblM = new JLabel("M", SwingConstants.CENTER);
        lblM.setForeground(Color.WHITE);
        lblM.setFont(new Font("Segoe UI", Font.BOLD, 16));
        circleM.add(lblM, BorderLayout.CENTER);
        
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        JLabel lblAdminName = new JLabel("Admin");
        lblAdminName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lblAdminRole = new JLabel("Admin Utama");
        lblAdminRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblAdminRole.setForeground(Color.GRAY);
        namePanel.add(lblAdminName);
        namePanel.add(lblAdminRole);
        
        profileBox.add(circleM, BorderLayout.WEST);
        profileBox.add(namePanel, BorderLayout.CENTER);
        actionPanel.add(btnTambahMember);
        actionPanel.add(btnCheckIn);
        actionPanel.add(profileBox);
        headerPanel.add(actionPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // --- Body ---
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // 1. Kartu Metrik (MENGGUNAKAN FLOATING ANIMATION & AUTOFIT AJI)
        JPanel cardsPanel = new JPanel(new GridBagLayout());
        cardsPanel.setOpaque(false);
        addMetricCard(cardsPanel, createFloatingMetricCard("Total Member", "351", "Member Reguler: 251   VIP: 100", "👥"), 0, 1.0);
        addMetricCard(cardsPanel, createFloatingMetricCard("Check-In Hari Ini", "184", "Naik 29% dari kemarin", "📅"), 1, 1.0);
        addMetricCard(cardsPanel, createFloatingMetricCard("Trainer Aktif", "16", "Semua shift terisi", "🏋"), 2, 0.7);
        addMetricCard(cardsPanel, createFloatingMetricCard("Pendapatan Bulanan", "Rp 54.820.000", "Target tercapai", "💰"), 3, 1.3);
        gbc.gridy = 0; gbc.weighty = 0.0; gbc.insets = new Insets(0, 0, 15, 0);
        bodyPanel.add(cardsPanel, gbc);

        // 2. Grafik
        JPanel chartsPanel = new JPanel(new GridBagLayout());
        chartsPanel.setOpaque(false);
        GridBagConstraints gbcChart = new GridBagConstraints();
        gbcChart.fill = GridBagConstraints.BOTH; gbcChart.weighty = 1.0;

        FloatingPanel lineChartShadow = new FloatingPanel(20);
        lineChartShadow.setLayout(new BorderLayout());
        RoundedPanel lineChartCard = new RoundedPanel(20, new Color(248, 248, 248));
        lineChartCard.setLayout(new BorderLayout());
        lineChartCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel titleLine = new JLabel("Tren Kunjungan & Pendaftaran (Bulanan)");
        titleLine.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lineChartCard.add(titleLine, BorderLayout.NORTH);
        lineChartShadow.add(lineChartCard, BorderLayout.CENTER);
        lineChartShadow.applyHoverRecursively(lineChartCard); 
        gbcChart.gridx = 0; gbcChart.weightx = 0.7; gbcChart.insets = new Insets(0, 0, 0, 15);
        chartsPanel.add(lineChartShadow, gbcChart);

        FloatingPanel pieChartShadow = new FloatingPanel(20);
        pieChartShadow.setLayout(new BorderLayout());
        RoundedPanel pieChartCard = new RoundedPanel(20, new Color(248, 248, 248));
        pieChartCard.setLayout(new BorderLayout());
        pieChartCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel titlePie = new JLabel("Member yg Datang Hari Ini");
        titlePie.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pieChartCard.add(titlePie, BorderLayout.NORTH);
        JPanel pieLegend = new JPanel(new GridLayout(2,1));
        pieLegend.setOpaque(false);
        pieLegend.add(new JLabel("🔵 Reguler Hadir (120)")); 
        pieLegend.add(new JLabel("🟢 VIP Hadir (64)"));
        pieChartCard.add(pieLegend, BorderLayout.SOUTH);
        pieChartShadow.add(pieChartCard, BorderLayout.CENTER);
        pieChartShadow.applyHoverRecursively(pieChartCard); 
        gbcChart.gridx = 1; gbcChart.weightx = 0.3; gbcChart.insets = new Insets(0, 0, 0, 0);
        chartsPanel.add(pieChartShadow, gbcChart);

        gbc.gridy = 1; gbc.weighty = 0.45;
        bodyPanel.add(chartsPanel, gbc);

        // 3. Tabel Dashboard (HASIL MERGE: TETAP MENGGUNAKAN DATABASE LU)
        ShadowPanel tableShadow = new ShadowPanel(20);
        tableShadow.setLayout(new BorderLayout());
        RoundedPanel tableCard = new RoundedPanel(20, Color.WHITE);
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel titleTable = new JLabel("Check-In Terbaru");
        titleTable.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableCard.add(titleTable, BorderLayout.NORTH);
        
        String[] kolom = {"No", "Nama", "ID Member", "Tipe Member", "Waktu Check-In", "Status"};
        
        // MANGGIL MESIN JOIN DATABASE LU
        Object[][] data = loadDataCheckInDariDB();
    
        tableCard.add(createCustomTable(data, kolom), BorderLayout.CENTER);
        tableShadow.add(tableCard, BorderLayout.CENTER);
        gbc.gridy = 2; gbc.weighty = 0.55; gbc.insets = new Insets(0, 0, 0, 0);
        bodyPanel.add(tableShadow, gbc);

        panel.add(bodyPanel, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // HALAMAN 2: DATA MEMBER 
    // =========================================================================
   private JPanel createDataMemberPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Header Judul dan Tombol Aksi di Atas
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Data Member");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblSubTitle = new JLabel("Kelola informasi, pendaftaran, dan status anggota gym.");
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubTitle.setForeground(new Color(100, 100, 100));
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubTitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        JTextField txtSearch = new JTextField(18);
        txtSearch.setPreferredSize(new Dimension(180, 40));

        JButton btnSearch = new JButton("Cari");
        btnSearch.setBackground(new Color(240, 240, 240));
        btnSearch.setPreferredSize(new Dimension(75, 40));
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnEdit = new JButton("Edit");
        btnEdit.setBackground(new Color(245, 158, 11));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit.setPreferredSize(new Dimension(80, 40));
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnHapus = new JButton("Hapus");
        btnHapus.setBackground(new Color(220, 53, 69));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHapus.setPreferredSize(new Dimension(85, 40));
        btnHapus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        actionPanel.add(txtSearch);
        actionPanel.add(btnSearch);
        actionPanel.add(btnEdit);
        actionPanel.add(btnHapus);
        headerPanel.add(actionPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Kotak Tabel Utama
        ShadowPanel tableShadow = new ShadowPanel(20);
        tableShadow.setLayout(new BorderLayout());
        RoundedPanel tableCard = new RoundedPanel(20, Color.WHITE);
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Nama Kolom Tabel
        String[] kolom = {"No", "ID Member", "Nama Lengkap", "No. HP", "Tipe Member", "Status"};

        // Model tabel (dikunci agar tidak bisa diedit langsung lewat double-click sel)
        DefaultTableModel modelMember = new DefaultTableModel(null, kolom) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblMember = new JTable(modelMember);

        // Styling tabel
        tblMember.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblMember.setRowHeight(38);
        tblMember.setShowGrid(false);
        tblMember.setIntercellSpacing(new Dimension(0, 0));
        tblMember.setSelectionBackground(new Color(230, 240, 250));
        tblMember.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = tblMember.getTableHeader();
        header.setBackground(new Color(10, 25, 60));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(100, 45));

        JScrollPane scrollPane = new JScrollPane(tblMember);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);
        tableShadow.add(tableCard, BorderLayout.CENTER);
        panel.add(tableShadow, BorderLayout.CENTER);

        // Muat data member aktif dari DataStore
        loadDataMemberKeTabel(modelMember);

        return panel;
    }

    // =========================================================================
    // HALAMAN LAINNYA 
    // =========================================================================
 private JPanel createKelasJadwalPanel() {
        // Panggil mesin database yang baru kita bikin di sini
        Object[][] dataJadwal = loadDataJadwalDariDB();

        // Masukin variabel dataJadwal ke dalam createGenericPage
        return createGenericPage(
                "Kelas & Jadwal", "Manajemen jadwal kelas kebugaran dan instruktur.",
                new String[]{"No", "Nama Kelas", "Instruktur", "Hari & Jam", "Kapasitas", "Status"},
                dataJadwal, 
                "+ Tambah Jadwal"
        );
    }
   
  private JPanel createManajemenTrainerPanel() {
        return createGenericPage(
            "Manajemen Trainer", 
            "Kelola jadwal, spesialisasi, dan data personal pelatih.",
            new String[]{"No", "ID Trainer", "Nama Trainer", "Spesialisasi", "No. HP", "Status"},
            loadDataTrainer(),
            "+ Tambah Trainer"
        );
    }

    private Object[][] loadDataTrainer() {
        java.util.List<String[]> listTrainer = DataStore.getInstance().getSemuaTrainer();
        Object[][] data = new Object[listTrainer.size()][6];

        for (int i = 0; i < listTrainer.size(); i++) {
            String[] t = listTrainer.get(i);
            data[i][0] = String.valueOf(i + 1); // Kolom No
            data[i][1] = t[0]; // ID Trainer (TRN-001)
            data[i][2] = t[1]; // Nama Trainer
            data[i][3] = t[2]; // Spesialisasi
            data[i][4] = t[3]; // No. HP
            data[i][5] = t[4]; // Status
        }
        return data;
    }
    
   private JPanel createManajemenPaketPanel() {
        return createGenericPage(
            "Paket & Transaksi", "Kelola daftar paket keanggotaan dan riwayat pembayaran.",
            new String[]{"No", "ID Transaksi", "Nama Member", "Paket", "Total Bayar", "Status"},
            loadDataTransaksi(),
            "+ Buat Paket"
        );
    }

    private Object[][] loadDataTransaksi() {
        java.util.List<Transaksi> listTrx = DataStore.getInstance().getSemuaTransaksi();
        Object[][] data = new Object[listTrx.size()][6];

        for (int i = 0; i < listTrx.size(); i++) {
            Transaksi t = listTrx.get(i);
            data[i][0] = String.valueOf(i + 1);
            data[i][1] = t.getIdTransaksi();
            data[i][2] = t.getNamaMember();
            data[i][3] = t.getPaketTeks();
            data[i][4] = Member.formatRupiah(t.getTotal());
            data[i][5] = t.getStatus();
        }
        return data;
    }

    private JPanel createRiwayatPresensiPanel() {
        return createGenericPage(
            "Riwayat Presensi", "Pantau catatan kehadiran member dan trainer secara real-time.",
            new String[]{"No", "ID Member", "Nama Lengkap", "Tipe", "Waktu Masuk", "Durasi Latihan"},
            new Object[][]{
                {"1", "MEM-001", "Budi Santoso", "Reguler", "05/11/2024 13:00", "1j 30m"},
                {"2", "MEM-002", "Andi Wijaya", "VIP", "05/11/2024 14:15", "45m"},
                {"3", "MEM-007", "Ayu Lestari", "VIP", "05/11/2024 16:00", "2j 10m"}
            },
            "Export Laporan"
        );
    }

    // =========================================================================
    // SIDEBAR & LOGIKA NAVIGASI
    // =========================================================================
    private void buildSidebar(JPanel rootPanel) {
        RoundedPanel sidebar = new RoundedPanel(30, new Color(10, 25, 60));
        sidebar.setPreferredSize(new Dimension(240, 0)); 
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel lblLogo = new JLabel("Fit Manage");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(Color.WHITE);
        
        // FITUR BARU AJI: LOGO
        Icon appLogo = AppLogo.icon(40);
        if (appLogo != null) {
            lblLogo.setIcon(appLogo);
            lblLogo.setIconTextGap(12);
        }
        sidebar.add(lblLogo, BorderLayout.NORTH);

        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);
        menuContainer.add(Box.createVerticalStrut(40));

        for (int i = 0; i < menus.length; i++) {
            final int menuIndex = i;
            final String menuName = menus[i];
            
            RoundedPanel menuItem = new RoundedPanel(15, new Color(0,0,0,0)); 
            menuItem.setLayout(new BorderLayout());
            menuItem.setMaximumSize(new Dimension(220, 40));
            menuItem.setBorder(new EmptyBorder(10, 15, 10, 15));

            JLabel lblMenu = new JLabel(menuName);
            lblMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblMenu.setForeground(new Color(200,200,200));
            menuItem.add(lblMenu, BorderLayout.CENTER);

            if (i == activeMenuIndex) {
                menuItem.setBgColor(new Color(0, 131, 176, 80));
                lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblMenu.setForeground(Color.WHITE);
            }
            
            menuItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (menuIndex != activeMenuIndex) {
                        menuItem.setBgColor(new Color(0, 131, 176, 40)); 
                        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        menuItem.repaint();
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (menuIndex != activeMenuIndex) {
                        menuItem.setBgColor(new Color(0, 0, 0, 0));
                        menuItem.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        menuItem.repaint();
                    }
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    activeMenuIndex = menuIndex; 
                    cardLayout.show(contentPanel, menuName);
                    
                    for (int j = 0; j < menuPanels.size(); j++) {
                        JPanel p = menuPanels.get(j);
                        if (j == activeMenuIndex) {
                            ((RoundedPanel)p).setBgColor(new Color(0, 131, 176, 80)); 
                            ((JLabel)p.getComponent(0)).setFont(new Font("Segoe UI", Font.BOLD, 13));
                            ((JLabel)p.getComponent(0)).setForeground(Color.WHITE);
                        } else {
                            ((RoundedPanel)p).setBgColor(new Color(0,0,0,0)); 
                            ((JLabel)p.getComponent(0)).setFont(new Font("Segoe UI", Font.PLAIN, 13));
                            ((JLabel)p.getComponent(0)).setForeground(new Color(200,200,200));
                        }
                        p.repaint();
                    }
                }
            });

            menuPanels.add(menuItem);
            menuContainer.add(menuItem);
            menuContainer.add(Box.createVerticalStrut(5));
        }
        sidebar.add(menuContainer, BorderLayout.CENTER);
        rootPanel.add(sidebar, BorderLayout.WEST);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private JPanel createGenericPage(String title, String subtitle, String[] kolom, Object[][] data, String btnText) {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel lblSubTitle = new JLabel(subtitle);
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubTitle.setForeground(new Color(100, 100, 100));
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubTitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);
        JTextField txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(200, 40));
        JButton btnSearch = new JButton("Cari");
        btnSearch.setBackground(new Color(240, 240, 240));
        btnSearch.setPreferredSize(new Dimension(80, 40));
        JButton btnAction = new JButton(btnText);
        btnAction.setBackground(new Color(0, 131, 176));
        btnAction.setForeground(Color.WHITE);
        btnAction.setPreferredSize(new Dimension(160, 40));
        actionPanel.add(txtSearch);
        actionPanel.add(btnSearch);
        actionPanel.add(btnAction);
        headerPanel.add(actionPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        ShadowPanel tableShadow = new ShadowPanel(20);
        tableShadow.setLayout(new BorderLayout());
        RoundedPanel tableCard = new RoundedPanel(20, Color.WHITE);
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableCard.add(createCustomTable(data, kolom), BorderLayout.CENTER);
        tableShadow.add(tableCard, BorderLayout.CENTER);
        panel.add(tableShadow, BorderLayout.CENTER);

        return panel;
    }

    private void addMetricCard(JPanel parent, JPanel card, int index, double weight) {
        card.setPreferredSize(new Dimension(0, card.getPreferredSize().height));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = index;
        c.gridy = 0;
        c.weightx = weight;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, index < 3 ? 15 : 0); 
        parent.add(card, c);
    }

    private JPanel createFloatingMetricCard(String title, String value, String subText, String iconSymbol) {
        FloatingPanel floatWrapper = new FloatingPanel(20);
        floatWrapper.setLayout(new BorderLayout());

        RoundedPanel card = new RoundedPanel(20, new Color(250, 250, 250));
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(80, 80, 80));
        
        JLabel lblValue = new AutoFitLabel(value, 12);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(new Color(20, 20, 20));
        JLabel lblSub = new JLabel(subText);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(new Color(120, 120, 120));

        textPanel.add(lblTitle); textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(lblValue); textPanel.add(Box.createVerticalGlue()); 
        textPanel.add(lblSub);
        card.add(textPanel, BorderLayout.CENTER);

        JLabel lblIcon = new JLabel(iconSymbol, SwingConstants.RIGHT);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblIcon.setVerticalAlignment(SwingConstants.TOP);
        card.add(lblIcon, BorderLayout.EAST);

        JPanel bottomBorder = new JPanel();
        bottomBorder.setBackground(new Color(220, 220, 220));
        bottomBorder.setPreferredSize(new Dimension(0, 2));
        card.add(bottomBorder, BorderLayout.SOUTH);

        floatWrapper.add(card, BorderLayout.CENTER);
        floatWrapper.applyHoverRecursively(card); 
        return floatWrapper;
    }

    private JScrollPane createCustomTable(Object[][] data, String[] kolom) {
        JTable table = new JTable(new DefaultTableModel(data, kolom));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setShowGrid(false); 
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(230, 240, 250)); 
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(10, 25, 60)); 
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(100, 45));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    // =========================================================================
    // INTEGRASI FORM TAMBAH MEMBER & CHECK-IN
    // =========================================================================
   private void openAddMemberDialog() {
        new AddMemberDialog(this).setVisible(true);
    }

    private void openCheckInDialog() {
        new CheckInDialog(this).setVisible(true);
    }
    
    private void openAddScheduleDialog() {
        FormDialog.tampilkan(this, new AddScheduleForm());
    }

    private void openAddTrainerDialog() {
        FormDialog.tampilkan(this, new AddTrainerForm());
    }
    
    private void openCreatePackageDialog() {
        FormDialog.tampilkan(this, new CreatePackageForm());
    }

   private void wireFeatures() {
        Container dashboardPage = (Container) contentPanel.getComponent(0);
        Container memberPage    = (Container) contentPanel.getComponent(1);
        Container kelasPage     = (Container) contentPanel.getComponent(2);
        Container trainerPage   = (Container) contentPanel.getComponent(3);
        Container paketPage     = (Container) contentPanel.getComponent(4);
        Container presensiPage  = (Container) contentPanel.getComponent(5);

        final DefaultTableModel checkInModel  = findTableModel(dashboardPage);
        final DefaultTableModel memberModel   = findTableModel(memberPage);    
        final DefaultTableModel presensiModel = findTableModel(presensiPage);
        final DefaultTableModel kelasModel    = findTableModel(kelasPage);  
        final DefaultTableModel trainerModel  = findTableModel(trainerPage);
        final DefaultTableModel paketModel    = findTableModel(paketPage);

        if (checkInModel == null || memberModel == null || presensiModel == null 
                || kelasModel == null || trainerModel == null || paketModel == null) return;

        final JTable tblMember = findTable(memberPage);
        final JTextField txtSearchMember = findTextField(memberPage);

        JButton btnTambahDiDataMember = findButton(memberPage, "+ Tambah Member");
        if (btnTambahDiDataMember != null) {
            btnTambahDiDataMember.addActionListener(e -> openAddMemberDialog());
        }

        // ==========================================
        // TOMBOL EDIT MEMBER (BUKA POP-UP EDIT)
        // ==========================================
        JButton btnEditMember = findButton(memberPage, "Edit");
        if (btnEditMember != null && tblMember != null) {
            btnEditMember.addActionListener(e -> {
                int row = tblMember.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this,
                            "Pilih baris member pada tabel terlebih dahulu!",
                            "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String idMember = memberModel.getValueAt(row, 1).toString();
                Member target = null;
                for (Member m : DataStore.getInstance().getSemuaMember()) {
                    if (m.getIdMember().equals(idMember)) {
                        target = m;
                        break;
                    }
                }

                if (target != null) {
                    new EditMemberDialog(this, target).setVisible(true);
                }
            });
        }

        // ==========================================
        // TOMBOL HAPUS MEMBER (SOFT DELETE -> NONAKTIF)
        // ==========================================
        JButton btnHapusMember = findButton(memberPage, "Hapus");
        if (btnHapusMember != null && tblMember != null) {
            btnHapusMember.addActionListener(e -> {
                int row = tblMember.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this,
                            "Pilih baris member yang ingin dihapus terlebih dahulu!",
                            "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String idMember = memberModel.getValueAt(row, 1).toString();
                String namaMember = memberModel.getValueAt(row, 2).toString();

                int konfirmasi = JOptionPane.showConfirmDialog(this,
                        "Yakin ingin menghapus member " + idMember + " (" + namaMember + ")?\n"
                        + "Member akan dinonaktifkan dari daftar aktif, namun riwayat transaksi & presensi tetap aman.",
                        "Konfirmasi Hapus Member",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (konfirmasi == JOptionPane.YES_OPTION) {
                    boolean sukses = DataStore.getInstance().nonaktifkanMember(idMember);
                    if (sukses) {
                        JOptionPane.showMessageDialog(this,
                                "Member " + idMember + " (" + namaMember + ") berhasil dihapus dari daftar aktif.",
                                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
        }

        // ==========================================
        // TOMBOL & KOTAK CARI MEMBER
        // ==========================================
    JButton btnCariMember = findButton(memberPage, "Cari");
        if (btnCariMember != null && txtSearchMember != null) {
            Runnable aksiCari = () -> {
                java.util.List<Member> hasil = DataStore.getInstance().cari(txtSearchMember.getText());
                memberModel.setRowCount(0);
                int no = 1;
                for (Member m : hasil) {
                    memberModel.addRow(new Object[]{
                        no++, m.getIdMember(), m.getNama(), m.getNoHp(),
                        m.getJenisMember(), m.getStatus().getLabel()
                    });
                }
            };
            btnCariMember.addActionListener(e -> aksiCari.run());
            txtSearchMember.addActionListener(e -> aksiCari.run());
            txtSearchMember.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    aksiCari.run();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    aksiCari.run();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    aksiCari.run();
                }
            });
        }
        
        // ==========================================
        // TOMBOL TAMBAH JADWAL DENGAN FITUR AUTO-REFRESH
        // ==========================================
        JButton btnTambahJadwal = findButton(kelasPage, "+ Tambah Jadwal");
        if (btnTambahJadwal != null) {
            btnTambahJadwal.addActionListener(e -> {
                openAddScheduleDialog();
                
                kelasModel.setRowCount(0);
                Object[][] dataBaru = loadDataJadwalDariDB();
                for (Object[] baris : dataBaru) {
                    kelasModel.addRow(baris);
                }
            });
        }

        // ==========================================
        // TOMBOL TAMBAH TRAINER DENGAN FITUR AUTO-REFRESH
        // ==========================================
        JButton btnTambahTrainer = findButton(trainerPage, "+ Tambah Trainer");
        if (btnTambahTrainer != null) {
            btnTambahTrainer.addActionListener(e -> {
                openAddTrainerDialog();
                
                trainerModel.setRowCount(0);
                Object[][] dataTrainerBaru = loadDataTrainer();
                for (Object[] baris : dataTrainerBaru) {
                    trainerModel.addRow(baris);
                }
            });
        }

        // ==========================================
        // TOMBOL BUAT PAKET DENGAN FITUR AUTO-REFRESH
        // ==========================================
        JButton btnBuatPaket = findButton(paketPage, "+ Buat Paket");
        if (btnBuatPaket != null) {
            btnBuatPaket.addActionListener(e -> {
                openCreatePackageDialog();

                paketModel.setRowCount(0);
                Object[][] dataTrxBaru = loadDataTransaksi();
                for (Object[] baris : dataTrxBaru) {
                    paketModel.addRow(baris);
                }
            });
        }

        DataStore.getInstance().addListener(new DataStore.Listener() {
            @Override
            public void onMemberAdded(Member m) {
                loadDataMemberKeTabel(memberModel);
            }

            @Override
            public void onMemberUpdated() {
                loadDataMemberKeTabel(memberModel);
            }

            @Override
            public void onCheckIn(Presensi p) {
                String waktu = p.getWaktuFormatted();
                checkInModel.insertRow(0, new Object[]{
                    "", p.getNama(), p.getIdMember(), p.getJenisMember(), waktu, p.getStatusMember()
                });
                renumber(checkInModel);
                presensiModel.insertRow(0, new Object[]{
                    "", p.getIdMember(), p.getNama(), p.getJenisMember(), waktu, "Sedang latihan"
                });
                renumber(presensiModel);
            }
        });
    }
   
   private JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) {
                return (JButton) c;
            }
            if (c instanceof Container) {
                JButton b = findButton((Container) c, text);
                if (b != null) return b;
            }
        }
        return null;
    }

    private DefaultTableModel findTableModel(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTable) {
                return (DefaultTableModel) ((JTable) c).getModel();
            }
            if (c instanceof Container) {
                DefaultTableModel m = findTableModel((Container) c);
                if (m != null) return m;
            }
        }
        return null;
    }

    private JTable findTable(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTable) {
                return (JTable) c;
            }
            if (c instanceof Container) {
                JTable t = findTable((Container) c);
                if (t != null) return t;
            }
        }
        return null;
    }

    private JTextField findTextField(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTextField) {
                return (JTextField) c;
            }
            if (c instanceof Container) {
                JTextField tf = findTextField((Container) c);
                if (tf != null) return tf;
            }
        }
        return null;
    }

    private void renumber(DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(String.valueOf(i + 1), i, 0);
        }
    }
    
    // ==========================================================
    // MESIN DATABASE 
    // ==========================================================
    private void loadDataMemberKeTabel(DefaultTableModel modelTabel) {
        modelTabel.setRowCount(0);
        int nomor = 1;
        for (Member m : DataStore.getInstance().getSemuaMember()) {
            modelTabel.addRow(new Object[]{
                nomor++,
                m.getIdMember(),
                m.getNama(),
                m.getNoHp(),
                m.getJenisMember(),
                m.getStatus().getLabel()
            });
        }
    }
    
    /**
     * Menarik data jadwal kelas dari basis data Supabase (tabel jadwal_kelas).
     * Metode ini memetakan kolom basis data ke dalam bentuk array objek
     * agar dapat langsung ditampilkan pada komponen tabel di antarmuka.
     */
    private Object[][] loadDataJadwalDariDB() {
        // Menyiapkan list penampung sementara untuk baris data jadwal
        java.util.List<Object[]> listData = new java.util.ArrayList<>();
        
        // Kueri SQL untuk mengambil data dari tabel jadwal_kelas di Supabase,
        // diurutkan berdasarkan Hari dan Jam Mulai secara ascending.
       // Kueri SQL untuk mengambil data dari tabel jadwal_kelas di Supabase,
        // diurutkan berdasarkan Hari dan Jam Mulai secara ascending.
        String query = "SELECT nama_kelas, instruktur, Hari, jam_mulai, kapasitas, COALESCE(peserta, 0) as peserta " +
                       "FROM jadwal_kelas " + 
                       "ORDER BY Hari ASC, jam_mulai ASC";
        // Membuka koneksi ke basis data secara aman menggunakan try-with-resources
        try (java.sql.Connection conn = KoneksiDB.sambungDB();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(query)) {
            
            int nomor = 1;

            // Melakukan perulangan untuk setiap baris data yang dikembalikan oleh basis data
            while (rs.next()) {
                String namaKelas = rs.getString("nama_kelas");
                
                // Mengambil data instruktur sesuai nama kolom aslinya di basis data
            String instruktur = rs.getString("instruktur");
                String hari = rs.getString("Hari");
                String jamMulai = rs.getString("jam_mulai"); // Format standar waktu (HH:MM:SS)
                int kapasitas = rs.getInt("kapasitas");
                int terisi = rs.getInt("peserta"); // Menggunakan kolom 'peserta' sebagai jumlah kapasitas terisi

                // Memotong format jam agar hanya menampilkan HH:MM (mengabaikan detik)
                if (jamMulai != null && jamMulai.length() >= 5) {
                    jamMulai = jamMulai.substring(0, 5);
                }

                // Menggabungkan hari dan jam menjadi satu teks tampilan
                String hariJam = hari + ", " + jamMulai;
                String formatKapasitas = terisi + "/" + kapasitas + " Orang";
                
                // Menentukan status ketersediaan kelas secara otomatis berdasarkan jumlah peserta
                String status = (terisi >= kapasitas) ? "Penuh" : "Tersedia";

                // Menyusun baris data ke dalam array objek
                Object[] baris = { String.valueOf(nomor++), namaKelas, instruktur, hariJam, formatKapasitas, status };
                listData.add(baris);
            }
            System.out.println("[Sistem] Data jadwal berhasil ditarik dari database.");
        } catch (java.sql.SQLException e) {
            System.err.println("[Sistem] Gagal menarik data jadwal. Detail Kesalahan: " + e.getMessage());
        }

        // Mengonversi list penampung menjadi array dua dimensi yang dibutuhkan oleh tabel
        return listData.toArray(new Object[0][0]);
    }

   private Object[][] loadDataCheckInDariDB() {
    java.util.List<Object[]> listData = new java.util.ArrayList<>();
    
    String query = "SELECT m.nama, m.id AS id_member, m.tipe_member, p.created_at, m.status " +
                   "FROM presensi p " +
                   "JOIN member m ON p.member_id = m.id " +
                   "ORDER BY p.created_at DESC " +
                   "LIMIT 10";

    try (java.sql.Connection conn = KoneksiDB.sambungDB();
         java.sql.Statement stmt = conn.createStatement();
         java.sql.ResultSet rs = stmt.executeQuery(query)) {
        
        int nomor = 1;
        // Formatter biar jamnya kelihatan rapi (contoh: 11 Nov 2026, 15:21)
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", java.util.Locale.forLanguageTag("id-ID"));

        while (rs.next()) {
            String nama = rs.getString("nama");
            String idMember = rs.getString("id_member"); 
            String tipe = rs.getString("tipe_member");
            String status = rs.getString("status");

            // Ambil sebagai Timestamp lalu ubah ke string berformat rapi
            java.sql.Timestamp tStamp = rs.getTimestamp("created_at");
            String waktuCheckIn = "-";
            if (tStamp != null) {
                waktuCheckIn = tStamp.toLocalDateTime().format(formatter);
            }

            Object[] baris = { nomor++, nama, idMember, tipe, waktuCheckIn, status };
            listData.add(baris);
        }
    } catch (java.sql.SQLException e) {
        System.err.println("[Sistem] Gagal menarik data check-in. Error: " + e.getMessage());
    }

    return listData.toArray(new Object[0][0]);
}

    public static void main(String args[]) {
        try {
            System.setProperty("flatlaf.useWindowDecorations", "true");
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("FlatLaf failed to initialize");
        }
        java.awt.EventQueue.invokeLater(() -> new Dashboard().setVisible(true));
    }
}

// ==========================================
// KELAS CUSTOM SHAPE & ANIMATION UI 
// ==========================================

class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color bgColor;
    public RoundedPanel(int radius, Color bgColor) {
        this.cornerRadius = radius;
        this.bgColor = bgColor;
        setOpaque(false);
    }
    public void setBgColor(Color newColor) { this.bgColor = newColor; repaint(); }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        g2.dispose();
    }
}

class ShadowPanel extends JPanel {
    private int radius;
    public ShadowPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 5, 5)); 
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(225, 225, 225)); 
        g2.fillRoundRect(3, 4, getWidth() - 5, getHeight() - 5, radius, radius);
        g2.dispose();
    }
}

class FloatingPanel extends JPanel {
    private static final int   LIFT              = 5;     
    private static final int   BLUR_REST         = 5;     
    private static final int   BLUR_HOVER        = 8;     
    private static final int   ALPHA_REST        = 26;  
    private static final int   ALPHA_HOVER       = 48;  
    private static final int   SHADOW_DROP_REST  = 3;     
    private static final int   SHADOW_DROP_HOVER = 6;     
    private static final float SPEED             = 0.22f; 

    private final int radius;
    private float progress = 0f;      
    private boolean hovered = false;

    private final Timer timer = new Timer(12, e -> animateStep());

    private final MouseAdapter hoverListener = new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) { setHover(true); }
        @Override public void mouseExited(MouseEvent e)  { checkMouseExit(); }
    };

    public FloatingPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
        setBorder(new EmptyBorder(
                LIFT + SHADOW_DROP_REST,          
                BLUR_HOVER,                       
                SHADOW_DROP_HOVER + BLUR_HOVER,   
                BLUR_HOVER));                     
        addMouseListener(hoverListener);
    }

    public void applyHoverRecursively(Component c) {
        c.addMouseListener(hoverListener);
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                applyHoverRecursively(child);
            }
        }
    }

    private void checkMouseExit() {
        PointerInfo info = MouseInfo.getPointerInfo();
        if (info == null) { setHover(false); return; }
        Point p = info.getLocation();
        SwingUtilities.convertPointFromScreen(p, this);
        if (!contains(p)) setHover(false);
    }

    private void setHover(boolean value) {
        if (hovered == value) return;
        hovered = value;
        setCursor(Cursor.getPredefinedCursor(value ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        if (!timer.isRunning()) timer.start();
    }

    private void animateStep() {
        float target = hovered ? 1f : 0f;
        progress += (target - progress) * SPEED;
        if (Math.abs(target - progress) < 0.01f) {
            progress = target;
            timer.stop();
        }
        repaint();
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Insets in = getInsets();
        int x = in.left;
        int y = in.top;
        int w = getWidth() - in.left - in.right;
        int h = getHeight() - in.top - in.bottom;

        int blur       = Math.round(lerp(BLUR_REST, BLUR_HOVER, progress));
        int alphaTotal = Math.round(lerp(ALPHA_REST, ALPHA_HOVER, progress));
        int drop       = Math.round(lerp(SHADOW_DROP_REST, SHADOW_DROP_HOVER, progress));
        int perLayer   = Math.max(1, alphaTotal / blur);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, perLayer));
        for (int i = blur; i >= 1; i--) {
            g2.fillRoundRect(x - i, y + drop - i, w + 2 * i, h + 2 * i,
                             radius + 2 * i, radius + 2 * i);
        }
        g2.dispose();
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(0, -Math.round(LIFT * progress));
        super.paintChildren(g2);
        g2.dispose();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }
}

class AutoFitLabel extends JLabel {
    private final float minSize;

    public AutoFitLabel(String text, int minSize) {
        super(text);
        this.minSize = minSize;
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, super.getPreferredSize().height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        String text = getText();
        if (text == null || text.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Insets in = getInsets();
        int available = getWidth() - in.left - in.right;

        Font font = getFont();
        float size = font.getSize2D();
        FontMetrics fm = g2.getFontMetrics(font);
        while (size > minSize && fm.stringWidth(text) > available) {
            size -= 1f;
            font = font.deriveFont(size);
            fm = g2.getFontMetrics(font);
        }

        g2.setFont(font);
        g2.setColor(getForeground());
        int y = in.top + (getHeight() - in.top - in.bottom - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, in.left, y);
        g2.dispose();
    }
}

class AppLogo {
    private static java.awt.image.BufferedImage original;
    private static boolean loaded = false;

    static Icon icon(int size) {
        java.awt.image.BufferedImage img = load();
        return (img == null) ? null : new LogoIcon(img, size);
    }

    private static java.awt.image.BufferedImage load() {
        if (!loaded) {
            loaded = true;
            try {
                java.net.URL url = AppLogo.class.getResource("/fitmanage/logo_fitManage.png");
                if (url != null) {
                    original = javax.imageio.ImageIO.read(url);
                } else {
                    System.err.println("logo_fitManage.png tidak ditemukan di package fitmanage");
                }
            } catch (Exception ex) {
                System.err.println("Gagal memuat logo: " + ex.getMessage());
            }
        }
        return original;
    }

    private static class LogoIcon implements Icon {
        private final java.awt.image.BufferedImage src;
        private final int size;
        private java.awt.image.BufferedImage cache;
        private int cachePx = -1;

        LogoIcon(java.awt.image.BufferedImage src, int size) {
            this.src = src;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            double scale = g2.getTransform().getScaleX();
            int px = Math.max(1, (int) Math.round(size * scale));
            if (cache == null || cachePx != px) {
                cache = scaleSmooth(src, px);
                cachePx = px;
            }
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(cache, x, y, size, size, null);
            g2.dispose();
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }

        private static java.awt.image.BufferedImage scaleSmooth(java.awt.image.BufferedImage img, int target) {
            int w = img.getWidth();
            while (w / 2 >= target) {
                w /= 2;
                img = resize(img, w);
            }
            return resize(img, target);
        }

        private static java.awt.image.BufferedImage resize(java.awt.image.BufferedImage img, int px) {
            java.awt.image.BufferedImage out =
                    new java.awt.image.BufferedImage(px, px, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(img, 0, 0, px, px, null);
            g2.dispose();
            return out;
        }
    }
    
    // ==========================================================
    // MESIN STATISTIK DASHBOARD DARI SUPABASE
    // ==========================================================
    private void muatStatistikDariDB() {
        int totalMember = 0;
        int regularCount = 0;
        int vipCount = 0;
        int checkInHariIni = 0;

        String qMember = "SELECT tipe_member, COUNT(*) as jumlah FROM member GROUP BY tipe_member";
        String qPresensi = "SELECT COUNT(*) as total FROM presensi WHERE DATE(created_at) = CURRENT_DATE";

        try (java.sql.Connection conn = KoneksiDB.sambungDB();
             java.sql.Statement stmt = conn.createStatement()) {
            
            // 1. Hitung Total Member & Tipe-nya
            try (java.sql.ResultSet rs = stmt.executeQuery(qMember)) {
                while (rs.next()) {
                    String tipe = rs.getString("tipe_member");
                    int jml = rs.getInt("jumlah");
                    totalMember += jml;
                    if (tipe != null && tipe.toLowerCase().contains("vip")) {
                        vipCount += jml;
                    } else {
                        regularCount += jml;
                    }
                }
            }

            // 2. Hitung Check-In Hari Ini dari Database
            try (java.sql.ResultSet rs = stmt.executeQuery(qPresensi)) {
                if (rs.next()) {
                    checkInHariIni = rs.getInt("total");
                }
            }

            System.out.println("[Sistem] Statistik dasbor berhasil dimuat: " + 
                               "Total Member = " + totalMember + ", Check-In Hari Ini = " + checkInHariIni);

        } catch (java.sql.SQLException e) {
            System.err.println("[Error] Gagal memuat statistik dashboard: " + e.getMessage());
        }
    }
    
}