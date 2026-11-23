package fitmanage;

import fitmanage.dao.KoneksiDB;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class Dashboard extends JFrame {

    private static final DateTimeFormatter FORMAT_TGL = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final List<JLabel> navLabels = new ArrayList<>();
    private int activeNavIndex = 0;

    // Komponen dinamis di halaman Dashboard
    private JLabel lblValTotalMember;
    private JLabel lblSubTotalMember;
    private JLabel lblValCheckIn;
    private JLabel lblValTrainer;
    private JLabel lblValPendapatan;
    private LineChartPanel comboChart;
    private PieChartPanel donutChart;

    // Model Tabel dinamis di seluruh tab
    private DefaultTableModel modelDashboardCheckIn;
    private DefaultTableModel modelMembers;
    private DefaultTableModel modelJadwal;
    private DefaultTableModel modelTrainer;
    private DefaultTableModel modelTransaksi;
    private DefaultTableModel modelPresensiFull;

    private JTextField txtSearchMember;
    private JComboBox<String> cmbFilterTipe;

    public Dashboard() {
        setTitle("Fit Manage - Sistem Manajemen Gym");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Rectangle desktopBounds = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        setBounds(desktopBounds);
        setMinimumSize(new Dimension(1024, 680));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        getContentPane().setBackground(ThemeUI.BG_MAIN);
        setLayout(new BorderLayout());

        // 1. Top Navbar
        add(createNavbar(), BorderLayout.NORTH);

        // 2. CardLayout untuk 6 menu navigasi
        contentCards.setBackground(ThemeUI.BG_MAIN);
        contentCards.add(createDashboardView(), "Dashboard");
        contentCards.add(createMemberView(), "Data Member");
        contentCards.add(createJadwalView(), "Kelas & Jadwal");
        contentCards.add(createTrainerView(), "Manajemen Trainer");
        contentCards.add(createTransaksiView(), "Manajemen Paket & Transaksi");
        contentCards.add(createPresensiView(), "Riwayat Presensi");
        add(contentCards, BorderLayout.CENTER);

        // Daftarkan listener agar setiap perubahan data langsung memperbarui UI
        DataStore.getInstance().addListener(new DataStore.Listener() {
            @Override public void onMemberAdded(Member member) { refreshAllData(); }
            @Override public void onMemberUpdated() { refreshAllData(); }
            @Override public void onCheckIn(Presensi presensi) { refreshAllData(); }
        });

        refreshAllData();
    }

    // =========================================================================
    // 1. TOP NAVIGATION BAR
    // =========================================================================
    private JPanel createNavbar() {
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(ThemeUI.BG_NAV);
        navbar.setPreferredSize(new Dimension(0, 62));
        navbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeUI.BORDER_CARD));

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 13));
        leftNav.setOpaque(false);
        leftNav.setBorder(new EmptyBorder(0, 14, 0, 0));

       JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);
        Icon navLogo = AppLogo.icon(32);
        if (navLogo != null) {
            brandPanel.add(new JLabel(navLogo));
        }
        JLabel lblBrand = new JLabel("Fit Manage");
        lblBrand.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblBrand.setForeground(ThemeUI.TEXT_WHITE);
        brandPanel.add(lblBrand);
        leftNav.add(brandPanel);

        leftNav.add(Box.createHorizontalStrut(6));

        String[] menus = {
            "Dashboard", "Data Member", "Kelas & Jadwal",
            "Manajemen Trainer", "Manajemen Paket & Transaksi", "Riwayat Presensi"
        };

        for (int i = 0; i < menus.length; i++) {
            final int index = i;
            final String menuName = menus[i];
            JLabel menuLabel = new JLabel(menuName);
            menuLabel.setFont(new Font("SansSerif", i == 0 ? Font.BOLD : Font.PLAIN, 13));
            menuLabel.setForeground(i == 0 ? ThemeUI.ACCENT_ORANGE : ThemeUI.TEXT_GRAY);
            menuLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            menuLabel.setBorder(new EmptyBorder(4, 2, 6, 2));

            menuLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    switchTab(index, menuName);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (activeNavIndex != index) menuLabel.setForeground(ThemeUI.TEXT_WHITE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (activeNavIndex != index) menuLabel.setForeground(ThemeUI.TEXT_GRAY);
                }
            });

            navLabels.add(menuLabel);
            leftNav.add(menuLabel);
        }

        // Profil Admin Kanan
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        rightNav.setOpaque(false);
        rightNav.setBorder(new EmptyBorder(0, 0, 0, 20));

        JPanel adminTextPanel = new JPanel();
        adminTextPanel.setLayout(new BoxLayout(adminTextPanel, BoxLayout.Y_AXIS));
        adminTextPanel.setOpaque(false);

        JLabel lblAdminName = new JLabel("Admin");
        lblAdminName.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblAdminName.setForeground(ThemeUI.TEXT_WHITE);
        lblAdminName.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel lblAdminRole = new JLabel("Admin Utama");
        lblAdminRole.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblAdminRole.setForeground(ThemeUI.TEXT_GRAY);
        lblAdminRole.setAlignmentX(Component.RIGHT_ALIGNMENT);

        adminTextPanel.add(lblAdminName);
        adminTextPanel.add(lblAdminRole);

        rightNav.add(adminTextPanel);
        rightNav.add(new ThemeUI.AvatarCircle("A", ThemeUI.DONUT_RED));

        navbar.add(leftNav, BorderLayout.WEST);
        navbar.add(rightNav, BorderLayout.EAST);
        return navbar;
    }

    private void switchTab(int index, String tabName) {
        activeNavIndex = index;
        for (int i = 0; i < navLabels.size(); i++) {
            JLabel lbl = navLabels.get(i);
            boolean active = (i == index);
            lbl.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 13));
            lbl.setForeground(active ? ThemeUI.ACCENT_ORANGE : ThemeUI.TEXT_GRAY);
        }
        cardLayout.show(contentCards, tabName);
    }

    // =========================================================================
    // TAB 1: DASHBOARD UTAMA
    // =========================================================================
    private JPanel createDashboardView() {
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(ThemeUI.BG_MAIN);
        mainContent.setBorder(new EmptyBorder(16, 28, 22, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        // Baris 0: Header Welcome Back
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 14, 0);
        mainContent.add(createHeaderSection(), gbc);

        // Baris 1: 4 Kartu KPI ringkas
        gbc.gridy = 1;
        gbc.weighty = 0.125;
        gbc.insets = new Insets(0, 0, 14, 0);
        mainContent.add(createMetricsRow(), gbc);

        // Baris 2: Grafik Bulanan & Donut Chart
        gbc.gridy = 2;
        gbc.weighty = 0.475;
        gbc.insets = new Insets(0, 0, 14, 0);
        mainContent.add(createChartsRow(), gbc);

        // Baris 3: Tabel Aktivitas Check-In Terbaru
        gbc.gridy = 3;
        gbc.weighty = 0.40;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainContent.add(createDashboardTableSection(), gbc);

        return mainContent;
    }

    private JPanel createHeaderSection() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);

        JLabel lblWelcome = new JLabel("Welcome Back!");
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblWelcome.setForeground(ThemeUI.TEXT_WHITE);

        JLabel lblSubtitle = new JLabel("Pantau dan kelola seluruh aktivitas gym Anda hari ini.");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblSubtitle.setForeground(ThemeUI.TEXT_GRAY);
        lblSubtitle.setBorder(new EmptyBorder(2, 0, 0, 0));

        titleGroup.add(lblWelcome);
        titleGroup.add(lblSubtitle);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonGroup.setOpaque(false);

        ThemeUI.RoundedButton btnTambah = new ThemeUI.RoundedButton("+ Tambah Member", new Color(36, 36, 36), ThemeUI.TEXT_WHITE, ThemeUI.BORDER_CARD);
        ThemeUI.RoundedButton btnCheckIn = new ThemeUI.RoundedButton("Check-in Hari Ini", ThemeUI.ACCENT_YELLOW, new Color(20, 20, 20), ThemeUI.ACCENT_YELLOW);

        btnTambah.addActionListener(e -> new AddMemberDialog(this).setVisible(true));
        btnCheckIn.addActionListener(e -> new CheckInDialog(this).setVisible(true));

        buttonGroup.add(btnTambah);
        buttonGroup.add(btnCheckIn);

        header.add(titleGroup, BorderLayout.WEST);
        header.add(buttonGroup, BorderLayout.EAST);
        return header;
    }

    private JPanel createMetricsRow() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);

        lblValTotalMember = new JLabel("0");
        lblSubTotalMember = new JLabel();
        grid.add(buildMetricCard("Total Member", lblValTotalMember, lblSubTotalMember, ThemeUI.IconType.USER));

        lblValCheckIn = new JLabel("0");
        JLabel lblSubCheckIn = new JLabel("<html><font color='#9B9B9B'>Data presensi </font><font color='#4ADE80'><b>hari ini</b></font></html>");
        grid.add(buildMetricCard("Check-In Hari Ini", lblValCheckIn, lblSubCheckIn, ThemeUI.IconType.CALENDAR));

        lblValTrainer = new JLabel("0");
        JLabel lblSubTrainer = new JLabel("<html><font color='#9B9B9B'>Semua shift terisi</font></html>");
        grid.add(buildMetricCard("Trainer Aktif", lblValTrainer, lblSubTrainer, ThemeUI.IconType.DUMBBELL));

        lblValPendapatan = new JLabel("Rp 0");
        JLabel lblSubPendapatan = new JLabel("<html><font color='#9B9B9B'>Total transaksi lunas</font></html>");
        grid.add(buildMetricCard("Pendapatan Paket", lblValPendapatan, lblSubPendapatan, ThemeUI.IconType.COINS));

        return grid;
    }

    private JPanel buildMetricCard(String title, JLabel valueLabel, JLabel subLabel, ThemeUI.IconType iconType) {
        RoundedPanel card = new RoundedPanel(12, ThemeUI.BG_CARD, ThemeUI.BORDER_CARD);
        card.setLayout(new BorderLayout(0, 2));
        card.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(210, 210, 210));
        topRow.add(lblTitle, BorderLayout.WEST);
        topRow.add(new ThemeUI.MetricIcon(iconType), BorderLayout.EAST);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(ThemeUI.TEXT_WHITE);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        card.add(topRow, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createChartsRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        RoundedPanel leftCard = new RoundedPanel(14, ThemeUI.BG_CARD, ThemeUI.BORDER_CARD);
        leftCard.setLayout(new BorderLayout());
        leftCard.setBorder(new EmptyBorder(16, 20, 14, 20));

        JLabel lblChartTitle = new JLabel("Tren Kunjungan & Pendaftaran (Bulanan)");
        lblChartTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblChartTitle.setForeground(ThemeUI.TEXT_WHITE);
        lblChartTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        comboChart = new LineChartPanel();
        leftCard.add(lblChartTitle, BorderLayout.NORTH);
        leftCard.add(comboChart, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.68;
        gbc.insets = new Insets(0, 0, 0, 16);
        row.add(leftCard, gbc);

        RoundedPanel rightCard = new RoundedPanel(14, ThemeUI.BG_CARD, ThemeUI.BORDER_CARD);
        rightCard.setLayout(new BorderLayout());
        rightCard.setBorder(new EmptyBorder(16, 20, 14, 20));

        JPanel rightHeader = new JPanel();
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.Y_AXIS));
        rightHeader.setOpaque(false);

        JLabel lblDonutTitle = new JLabel("Kehadiran Member");
        lblDonutTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblDonutTitle.setForeground(ThemeUI.TEXT_WHITE);

        JLabel lblDonutSub = new JLabel("Komposisi check-in hari ini");
        lblDonutSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblDonutSub.setForeground(ThemeUI.TEXT_GRAY);
        lblDonutSub.setBorder(new EmptyBorder(2, 0, 0, 0));

        rightHeader.add(lblDonutTitle);
        rightHeader.add(lblDonutSub);

        donutChart = new PieChartPanel();
        rightCard.add(rightHeader, BorderLayout.NORTH);
        rightCard.add(donutChart, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.32;
        gbc.insets = new Insets(0, 0, 0, 0);
        row.add(rightCard, gbc);

        return row;
    }

    private JPanel createDashboardTableSection() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JLabel lblSection = new JLabel("Check-In Terbaru");
        lblSection.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblSection.setForeground(ThemeUI.TEXT_WHITE);
        wrapper.add(lblSection, BorderLayout.NORTH);

        RoundedPanel tableCard = new RoundedPanel(14, ThemeUI.BG_CARD, ThemeUI.BORDER_CARD);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblInnerTitle = new JLabel("Aktivitas Check-In Terbaru");
        lblInnerTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblInnerTitle.setForeground(ThemeUI.TEXT_WHITE);
        lblInnerTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        tableCard.add(lblInnerTitle, BorderLayout.NORTH);

        String[] columns = {"No", "Nama", "ID Member", "Tipe Member", "Waktu Check-In", "Status"};
        modelDashboardCheckIn = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(modelDashboardCheckIn);
        ThemeUI.styleDarkTable(table, 5);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        tableCard.add(ThemeUI.wrapDarkScroll(table), BorderLayout.CENTER);

        wrapper.add(tableCard, BorderLayout.CENTER);
        return wrapper;
    }

    // =========================================================================
    // TAB 2: DATA MEMBER
    // =========================================================================
    private JPanel createMemberView() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(ThemeUI.BG_MAIN);
        page.setBorder(new EmptyBorder(20, 28, 24, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Manajemen Data Member");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ThemeUI.TEXT_WHITE);
        top.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        txtSearchMember = ThemeUI.createDarkTextField("");
        txtSearchMember.setPreferredSize(new Dimension(220, 36));
        txtSearchMember.setToolTipText("Cari nama atau ID member...");
        txtSearchMember.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterMemberTable(); }
        });

        cmbFilterTipe = ThemeUI.createDarkComboBox(new String[]{"Semua Tipe", "Reguler", "VIP", "Personal Trainer"});
        cmbFilterTipe.setPreferredSize(new Dimension(160, 36));
        cmbFilterTipe.addActionListener(e -> filterMemberTable());

        ThemeUI.RoundedButton btnTambah = new ThemeUI.RoundedButton("+ Tambah Member", ThemeUI.ACCENT_ORANGE, new Color(20, 20, 20), ThemeUI.ACCENT_ORANGE);
        btnTambah.addActionListener(e -> new AddMemberDialog(this).setVisible(true));

        controls.add(txtSearchMember);
        controls.add(cmbFilterTipe);
        controls.add(btnTambah);
        top.add(controls, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(14, ThemeUI.BG_CARD, ThemeUI.BORDER_CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] cols = {"No", "ID Member", "Nama Lengkap", "Tipe Member", "No. HP", "Tanggal Daftar", "Status"};
        modelMembers = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(modelMembers);
        ThemeUI.styleDarkTable(table, 6);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        card.add(ThemeUI.wrapDarkScroll(table), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setOpaque(false);

        ThemeUI.RoundedButton btnCheckInSelected = new ThemeUI.RoundedButton("Check-In Member Terpilih", ThemeUI.ACCENT_YELLOW, new Color(20, 20, 20), ThemeUI.ACCENT_YELLOW);
        ThemeUI.RoundedButton btnEdit = new ThemeUI.RoundedButton("Edit Member", new Color(45, 45, 45), ThemeUI.TEXT_WHITE, ThemeUI.BORDER_CARD);
        ThemeUI.RoundedButton btnHapus = new ThemeUI.RoundedButton("Hapus Member Terpilih", new Color(55, 28, 28), ThemeUI.TEXT_WHITE, new Color(110, 45, 45));

        btnCheckInSelected.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih baris member pada tabel terlebih dahulu.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            String id = (String) modelMembers.getValueAt(modelRow, 1);
            for (Member m : DataStore.getInstance().getSemuaMember()) {
                if (m.getIdMember().equals(id)) {
                    Presensi hasil = DataStore.getInstance().catatPresensi(m);
                    if (hasil != null) {
                        JOptionPane.showMessageDialog(this, m.getNama() + " berhasil Check-In!");
                    } else {
                        JOptionPane.showMessageDialog(this, m.getNama() + " sudah check-in hari ini atau paket tidak aktif.", "Info", JOptionPane.WARNING_MESSAGE);
                    }
                    break;
                }
            }
        });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih baris member yang ingin diedit terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            String idMember = modelMembers.getValueAt(modelRow, 1).toString();
            for (Member m : DataStore.getInstance().getSemuaMember()) {
                if (m.getIdMember().equals(idMember)) {
                    new EditMemberDialog(this, m).setVisible(true);
                    break;
                }
            }
        });

        btnHapus.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih baris member yang ingin dihapus terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            String idMember = modelMembers.getValueAt(modelRow, 1).toString();
            String namaMember = modelMembers.getValueAt(modelRow, 2).toString();

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

        bottomBar.add(btnCheckInSelected);
        bottomBar.add(btnEdit);
        bottomBar.add(btnHapus);
        card.add(bottomBar, BorderLayout.SOUTH);

        page.add(card, BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // TAB 3: KELAS & JADWAL
    // =========================================================================
    private JPanel createJadwalView() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(ThemeUI.BG_MAIN);
        page.setBorder(new EmptyBorder(20, 28, 24, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Kelas & Jadwal Latihan Gym");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ThemeUI.TEXT_WHITE);
        top.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JTextField txtSearch = ThemeUI.createDarkTextField("");
        txtSearch.setPreferredSize(new Dimension(220, 36));
        txtSearch.setToolTipText("Cari kelas, instruktur, atau hari...");

        ThemeUI.RoundedButton btnAdd = new ThemeUI.RoundedButton("+ Tambah Jadwal Kelas", ThemeUI.ACCENT_ORANGE, new Color(20, 20, 20), ThemeUI.ACCENT_ORANGE);
        btnAdd.addActionListener(e -> {
            FormDialog.tampilkan(this, new AddScheduleForm());
            refreshJadwalTable();
        });

        controls.add(txtSearch);
        controls.add(btnAdd);
        top.add(controls, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(14, ThemeUI.BG_CARD, ThemeUI.BORDER_CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] cols = {"No", "Nama Kelas", "Instruktur", "Hari & Jam", "Kapasitas", "Status"};
        modelJadwal = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelJadwal);
        ThemeUI.styleDarkTable(table, 5);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        aktifkanSearchTabel(txtSearch, table);

        card.add(ThemeUI.wrapDarkScroll(table), BorderLayout.CENTER);
        page.add(card, BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // TAB 4: MANAJEMEN TRAINER
    // =========================================================================
    private JPanel createTrainerView() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(ThemeUI.BG_MAIN);
        page.setBorder(new EmptyBorder(20, 28, 24, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Manajemen Personal Trainer");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ThemeUI.TEXT_WHITE);
        top.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JTextField txtSearch = ThemeUI.createDarkTextField("");
        txtSearch.setPreferredSize(new Dimension(220, 36));
        txtSearch.setToolTipText("Cari nama, ID, atau spesialisasi trainer...");

        ThemeUI.RoundedButton btnAdd = new ThemeUI.RoundedButton("+ Tambah Trainer", ThemeUI.ACCENT_ORANGE, new Color(20, 20, 20), ThemeUI.ACCENT_ORANGE);
        btnAdd.addActionListener(e -> {
            FormDialog.tampilkan(this, new AddTrainerForm());
            refreshAllData();
        });

        controls.add(txtSearch);
        controls.add(btnAdd);
        top.add(controls, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(14, ThemeUI.BG_CARD, ThemeUI.BORDER_CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] cols = {"No", "ID Trainer", "Nama Trainer", "Spesialisasi", "No. HP", "Status"};
        modelTrainer = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelTrainer);
        ThemeUI.styleDarkTable(table, 5);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        aktifkanSearchTabel(txtSearch, table);

        card.add(ThemeUI.wrapDarkScroll(table), BorderLayout.CENTER);
        page.add(card, BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // TAB 5: MANAJEMEN PAKET & TRANSAKSI
    // =========================================================================
    private JPanel createTransaksiView() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(ThemeUI.BG_MAIN);
        page.setBorder(new EmptyBorder(20, 28, 24, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Manajemen Paket & Transaksi Pembayaran");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ThemeUI.TEXT_WHITE);
        top.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JTextField txtSearch = ThemeUI.createDarkTextField("");
        txtSearch.setPreferredSize(new Dimension(220, 36));
        txtSearch.setToolTipText("Cari ID transaksi, member, atau paket...");

        ThemeUI.RoundedButton btnAdd = new ThemeUI.RoundedButton("+ Buat Transaksi Baru", ThemeUI.ACCENT_YELLOW, new Color(20, 20, 20), ThemeUI.ACCENT_YELLOW);
        btnAdd.addActionListener(e -> {
            FormDialog.tampilkan(this, new CreatePackageForm());
            refreshAllData();
        });

        controls.add(txtSearch);
        controls.add(btnAdd);
        top.add(controls, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(14, ThemeUI.BG_CARD, ThemeUI.BORDER_CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] cols = {"No", "ID Transaksi", "Nama Member", "Paket Membership", "Total Bayar", "Tanggal", "Metode", "Status"};
        modelTransaksi = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelTransaksi);
        ThemeUI.styleDarkTable(table, 7);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        aktifkanSearchTabel(txtSearch, table);

        card.add(ThemeUI.wrapDarkScroll(table), BorderLayout.CENTER);
        page.add(card, BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // TAB 6: RIWAYAT PRESENSI
    // =========================================================================
    private JPanel createPresensiView() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(ThemeUI.BG_MAIN);
        page.setBorder(new EmptyBorder(20, 28, 24, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Riwayat Presensi & Log Check-In");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ThemeUI.TEXT_WHITE);
        top.add(title, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JTextField txtSearch = ThemeUI.createDarkTextField("");
        txtSearch.setPreferredSize(new Dimension(220, 36));
        txtSearch.setToolTipText("Cari nama, ID member, atau waktu check-in...");

        ThemeUI.RoundedButton btnExport = new ThemeUI.RoundedButton("Export Laporan", new Color(36, 36, 36), ThemeUI.TEXT_WHITE, ThemeUI.BORDER_CARD);
        ThemeUI.RoundedButton btnCheckIn = new ThemeUI.RoundedButton("Check-in Hari Ini", ThemeUI.ACCENT_YELLOW, new Color(20, 20, 20), ThemeUI.ACCENT_YELLOW);

        btnExport.addActionListener(e -> exportLaporanPresensi());
        btnCheckIn.addActionListener(e -> new CheckInDialog(this).setVisible(true));

        btns.add(txtSearch);
        btns.add(btnExport);
        btns.add(btnCheckIn);
        top.add(btns, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(14, ThemeUI.BG_CARD, ThemeUI.BORDER_CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] cols = {"No", "Nama", "ID Member", "Tipe Member", "Waktu Check-In", "Status"};
        modelPresensiFull = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelPresensiFull);
        ThemeUI.styleDarkTable(table, 5);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        aktifkanSearchTabel(txtSearch, table);

        card.add(ThemeUI.wrapDarkScroll(table), BorderLayout.CENTER);
        page.add(card, BorderLayout.CENTER);
        return page;
    }

    // =========================================================================
    // HELPER SEARCH TABEL & EXPORT LAPORAN (3 FORMAT)
    // =========================================================================
    private void aktifkanSearchTabel(JTextField txtSearch, JTable table) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        Runnable filterAksi = () -> {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
            }
        };

        txtSearch.addActionListener(e -> filterAksi.run());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterAksi.run(); }
            @Override public void removeUpdate(DocumentEvent e) { filterAksi.run(); }
            @Override public void changedUpdate(DocumentEvent e) { filterAksi.run(); }
        });
    }

    private void exportLaporanPresensi() {
        String[] opsi = {"Excel (.xlsx)", "CSV (.csv)", "HTML / PDF (.html)"};
        int pilih = JOptionPane.showOptionDialog(
                this,
                "Pilih format file laporan presensi:",
                "Export Laporan",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, opsi, opsi[0]
        );
        if (pilih < 0) return;

        String ekstensi = (pilih == 0) ? ".xlsx" : (pilih == 1) ? ".csv" : ".html";
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Laporan Presensi");
        chooser.setSelectedFile(new File("Laporan_Presensi_FitManage" + ekstensi));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(ekstensi)) {
                file = new File(file.getAbsolutePath() + ekstensi);
            }
            try {
                if (pilih == 0) {
                    ReportExporter.tulisXlsx(file, "Laporan Riwayat Presensi", modelPresensiFull);
                } else if (pilih == 1) {
                    ReportExporter.tulisCsv(file, modelPresensiFull);
                } else {
                    ReportExporter.tulisHtml(file, "Laporan Riwayat Presensi", modelPresensiFull);
                }
                JOptionPane.showMessageDialog(this,
                        "Laporan berhasil disimpan ke:\n" + file.getAbsolutePath(),
                        "Export Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Gagal export laporan: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // REFRESH SEMUA DATA UI DARI DATASTORE & SUPABASE
    // =========================================================================
    private void refreshAllData() {
        DataStore store = DataStore.getInstance();

        // Hitung statistik member dari DataStore
        List<Member> allMembers = store.getSemuaMember();
        int totalMember = allMembers.size();
        int totalVip = 0;
       for (Member m : allMembers) {
            if (m instanceof MemberVIP || m instanceof PersonalTrainer) {
                totalVip++;
            }
        }
        int totalReguler = totalMember - totalVip;

        // Hitung trainer aktif
        List<String[]> allTrainers = store.getSemuaTrainer();
        int trainerAktif = 0;
        for (String[] t : allTrainers) {
            if ("Aktif".equalsIgnoreCase(t[4])) trainerAktif++;
        }

        // Hitung pendapatan dari transaksi lunas
        List<Transaksi> allTransaksi = store.getSemuaTransaksi();
        long totalPendapatan = 0;
        for (Transaksi tr : allTransaksi) {
            if (tr.isLunas()) totalPendapatan += tr.getTotal();
        }

        int checkInHariIni = store.getCheckInRegulerHariIni() + store.getCheckInVipHariIni();

        // Update KPI Cards
        lblValTotalMember.setText(String.valueOf(totalMember));
        lblSubTotalMember.setText("<html><font color='#9B9B9B'>Member Reguler: </font><font color='#F5F5F5'><b>"
                + totalReguler + "</b></font>&nbsp;&nbsp;<font color='#9B9B9B'>VIP: </font><font color='#F5F5F5'><b>"
                + totalVip + "</b></font></html>");
        lblValCheckIn.setText(String.valueOf(checkInHariIni));
        lblValTrainer.setText(String.valueOf(trainerAktif));
        lblValPendapatan.setText(Member.formatRupiah(totalPendapatan));

        // Repaint Charts
        if (comboChart != null) comboChart.repaint();
        if (donutChart != null) donutChart.repaint();

        // Update Tabel Check-In Dashboard & Full Presensi dari Supabase
        modelDashboardCheckIn.setRowCount(0);
        modelPresensiFull.setRowCount(0);
        for (Object[] baris : loadDataCheckInDariDB()) {
            modelDashboardCheckIn.addRow(baris);
            modelPresensiFull.addRow(baris);
        }

        // Update Tabel Member
        filterMemberTable();

        // Update Tabel Jadwal dari Supabase
        refreshJadwalTable();

        // Update Tabel Trainer
        modelTrainer.setRowCount(0);
        for (int i = 0; i < allTrainers.size(); i++) {
            String[] t = allTrainers.get(i);
            modelTrainer.addRow(new Object[]{
                String.valueOf(i + 1), t[0], t[1], t[2], t[3], t[4]
            });
        }

        // Update Tabel Transaksi
        modelTransaksi.setRowCount(0);
        for (int i = 0; i < allTransaksi.size(); i++) {
            Transaksi tr = allTransaksi.get(i);
            String tgl = (tr.getWaktu() != null) ? tr.getWaktu().format(FORMAT_WAKTU) : "-";
            modelTransaksi.addRow(new Object[]{
                String.valueOf(i + 1), tr.getIdTransaksi(), tr.getNamaMember(), tr.getPaketTeks(),
                Member.formatRupiah(tr.getTotal()), tgl, tr.getMetode(), tr.getStatus()
            });
        }
    }

    private void refreshJadwalTable() {
        if (modelJadwal == null) return;
        modelJadwal.setRowCount(0);
        for (Object[] baris : loadDataJadwalDariDB()) {
            modelJadwal.addRow(baris);
        }
    }

    private void filterMemberTable() {
        if (modelMembers == null) return;
        modelMembers.setRowCount(0);
        String keyword = txtSearchMember != null ? txtSearchMember.getText().trim().toLowerCase() : "";
        String tipeFilter = cmbFilterTipe != null ? (String) cmbFilterTipe.getSelectedItem() : "Semua Tipe";

        int no = 1;
        for (Member m : DataStore.getInstance().getSemuaMember()) {
            boolean matchKey = keyword.isEmpty()
                    || m.getNama().toLowerCase().contains(keyword)
                    || m.getIdMember().toLowerCase().contains(keyword);
            boolean matchTipe = "Semua Tipe".equals(tipeFilter)
                    || m.getJenisMember().equalsIgnoreCase(tipeFilter);

            if (matchKey && matchTipe) {
                String tglDaftar = (m.getTanggalDaftar() != null) ? m.getTanggalDaftar().format(FORMAT_TGL) : "-";
                modelMembers.addRow(new Object[]{
                    String.valueOf(no++), m.getIdMember(), m.getNama(), m.getJenisMember(),
                    m.getNoHp(), tglDaftar, m.getStatus().getLabel()
                });
            }
        }
    }

    // =========================================================================
    // MESIN QUERY DATABASE SUPABASE
    // =========================================================================
    private Object[][] loadDataJadwalDariDB() {
        List<Object[]> listData = new ArrayList<>();
        String query = "SELECT nama_kelas, instruktur, Hari, jam_mulai, kapasitas, COALESCE(peserta, 0) as peserta " +
                       "FROM jadwal_kelas " +
                       "ORDER BY Hari ASC, jam_mulai ASC";

        try (Connection conn = KoneksiDB.sambungDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int nomor = 1;
            while (rs.next()) {
                String namaKelas = rs.getString("nama_kelas");
                String instruktur = rs.getString("instruktur");
                String hari = rs.getString("Hari");
                String jamMulai = rs.getString("jam_mulai");
                int kapasitas = rs.getInt("kapasitas");
                int terisi = rs.getInt("peserta");

                if (jamMulai != null && jamMulai.length() >= 5) {
                    jamMulai = jamMulai.substring(0, 5);
                }

                String hariJam = hari + ", " + jamMulai;
                String formatKapasitas = terisi + "/" + kapasitas + " Orang";
                String status = (terisi >= kapasitas) ? "Penuh" : "Tersedia";

                listData.add(new Object[]{String.valueOf(nomor++), namaKelas, instruktur, hariJam, formatKapasitas, status});
            }
        } catch (SQLException e) {
            System.err.println("[Sistem] Gagal menarik data jadwal. Detail Kesalahan: " + e.getMessage());
        }

        return listData.toArray(new Object[0][0]);
    }

    private Object[][] loadDataCheckInDariDB() {
        List<Object[]> listData = new ArrayList<>();
        String query = "SELECT m.nama, m.id AS id_member, m.tipe_member, p.created_at, m.status " +
                       "FROM presensi p " +
                       "JOIN member m ON p.member_id = m.id " +
                       "ORDER BY p.created_at DESC";

        try (Connection conn = KoneksiDB.sambungDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int nomor = 1;
            while (rs.next()) {
                String nama = rs.getString("nama");
                String idMember = rs.getString("id_member");
                String tipe = rs.getString("tipe_member");
                String status = rs.getString("status");

                Timestamp tStamp = rs.getTimestamp("created_at");
                String waktuCheckIn = (tStamp != null) ? tStamp.toLocalDateTime().format(FORMAT_WAKTU) : "-";

                listData.add(new Object[]{String.valueOf(nomor++), nama, idMember, tipe, waktuCheckIn, status});
            }
        } catch (SQLException e) {
            System.err.println("[Sistem] Gagal menarik data check-in. Error: " + e.getMessage());
        }

        return listData.toArray(new Object[0][0]);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
    }
}