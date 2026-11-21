package fitmanage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Jendela PRATINJAU Batch 1 (data dummy, TIDAK menyentuh database).
 * Jalankan file ini (Shift+F6) untuk melihat tema, kartu KPI, chart, dan tabel pill
 * sebelum kita sambungkan ke Dashboard asli di Batch 2. Aman dihapus setelah selesai.
 */
public class ThemePreview {

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(ThemePreview::tampil);
    }

    private static void tampil() {
        JFrame f = new JFrame("Fit Manage - Pratinjau Tema (Batch 1)");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        f.setContentPane(root);

        String[] menu = {"Dashboard", "Data Member", "Kelas & Jadwal", "Manajemen Trainer",
            "Manajemen Paket & Transaksi", "Riwayat Presensi"};
        root.add(new TopNavBar(menu, i -> System.out.println("Tab dipilih: " + menu[i])), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(22, 36, 24, 36));
        root.add(body, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.weightx = 1;

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel judul = new JPanel(new GridLayout(2, 1, 0, 2));
        judul.setOpaque(false);
        judul.add(Theme.label("Welcome Back!", Font.BOLD, 30, Theme.TEXT));
        judul.add(Theme.label("Pantau dan kelola seluruh aktivitas gym Anda hari ini.", Font.PLAIN, 14, Theme.TEXT_DIM));
        JPanel aksi = new JPanel();
        aksi.setOpaque(false);
        JButton b1 = new JButton("+ Tambah Member");
        Theme.tombolGelap(b1);
        JButton b2 = new JButton("Check-In Hari Ini");
        Theme.tombolUtama(b2);
        aksi.add(b1);
        aksi.add(b2);
        header.add(judul, BorderLayout.WEST);
        header.add(aksi, BorderLayout.EAST);
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 18, 0);
        body.add(header, c);

        // Baris 1: KPI
        JPanel kpi = new JPanel(new GridLayout(1, 4, 16, 0));
        kpi.setOpaque(false);
        StatCard k1 = new StatCard("Total Member", UiIcons.Type.USER);
        k1.setValue("351");
        k1.setSub("Member Reguler: 251   VIP: 100");
        StatCard k2 = new StatCard("Check-In Hari Ini", UiIcons.Type.CALENDAR);
        k2.setValue("184");
        k2.setSub("<html>Naik <font color='#4ADE80'>29%</font> dari kemarin</html>");
        StatCard k3 = new StatCard("Trainer Aktif", UiIcons.Type.DUMBBELL);
        k3.setValue("16");
        k3.setSub("Semua shift terisi");
        StatCard k4 = new StatCard("Pendapatan Bulanan", UiIcons.Type.COIN);
        k4.setValue("Rp 54.820.000");
        k4.setSub("Target tercapai");
        kpi.add(k1);
        kpi.add(k2);
        kpi.add(k3);
        kpi.add(k4);
        c.gridy = 1;
        body.add(kpi, c);

        // Baris 2: chart (68% / 32%)
        JPanel charts = new JPanel(new GridBagLayout());
        charts.setOpaque(false);
        GridBagConstraints cc = new GridBagConstraints();
        cc.fill = GridBagConstraints.BOTH;
        cc.gridy = 0;
        cc.weighty = 1;

        CardPanel kiri = new CardPanel();
        kiri.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));
        kiri.add(Theme.label("Tren Kunjungan & Pendaftaran (Bulanan)", Font.BOLD, 17, Theme.TEXT), BorderLayout.NORTH);
        TrendChart tren = new TrendChart();
        tren.setData(new int[]{23, 32, 38, 28, 34, 36, 46, 52, 52, 49, 56, 44},
                new int[]{14, 20, 26, 16, 27, 22, 36, 24, 34, 25, 31, 13});
        kiri.add(tren, BorderLayout.CENTER);
        cc.gridx = 0;
        cc.weightx = 0.68;
        cc.insets = new Insets(0, 0, 0, 16);
        charts.add(kiri, cc);

        CardPanel kanan = new CardPanel();
        kanan.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));
        JPanel tk = new JPanel(new GridLayout(2, 1, 0, 4));
        tk.setOpaque(false);
        tk.add(Theme.label("Kehadiran Member", Font.BOLD, 17, Theme.TEXT));
        tk.add(Theme.label("Komposisi check-in hari ini", Font.PLAIN, 13, Theme.TEXT_DIM));
        kanan.add(tk, BorderLayout.NORTH);
        DonutChart donat = new DonutChart();
        donat.setData(120, 64);
        kanan.add(donat, BorderLayout.CENTER);
        cc.gridx = 1;
        cc.weightx = 0.32;
        cc.insets = new Insets(0, 0, 0, 0);
        charts.add(kanan, cc);

        c.gridy = 2;
        c.weighty = 0.55;
        c.insets = new Insets(0, 0, 18, 0);
        body.add(charts, c);

        // Baris 3: tabel
        CardPanel tabel = new CardPanel();
        tabel.setBorder(BorderFactory.createEmptyBorder(18, 22, 16, 22));
        tabel.add(Theme.label("Aktivitas Check-In Terbaru", Font.BOLD, 17, Theme.TEXT), BorderLayout.NORTH);
        String[] kolom = {"No", "Nama", "ID Member", "Tipe Member", "Waktu Check-In", "Status"};
        Object[][] data = {
            {1, "alfa", "MEM-004", "Reguler", "14 Des 2026, 09:52", "Aktif"},
            {2, "sekarang ang ang", "MEM-010", "Personal Trainer", "19 Nov 2026, 12:48", "Aktif"},
            {3, "zaky", "MEM-009", "Personal Trainer", "18 Nov 2026, 22:38", "Hampir Habis"},
            {4, "budi", "MEM-001", "VIP", "17 Nov 2026, 08:10", "Kedaluwarsa"}
        };
        JTable tbl = new JTable(new DefaultTableModel(data, kolom) {
            @Override
            public boolean isCellEditable(int r, int cc2) {
                return false;
            }
        });
        JScrollPane sp = Theme.gayaTabel(tbl);
        tbl.getColumnModel().getColumn(0).setMaxWidth(70);
        tbl.getColumnModel().getColumn(5).setCellRenderer(new StatusPillRenderer());
        sp.setPreferredSize(new Dimension(100, 170));
        tabel.add(sp, BorderLayout.CENTER);
        c.gridy = 3;
        c.weighty = 0.45;
        c.insets = new Insets(0, 0, 0, 0);
        body.add(tabel, c);

        f.setSize(1440, 900);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
