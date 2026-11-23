package fitmanage;

import fitmanage.dao.AdminDAO;
import fitmanage.dao.AdminDAOImpl;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {

    private static final Color NAVY  = new Color(10, 25, 60);
    private static final Color BLUE  = new Color(0, 131, 176);
    private static final Color RED   = new Color(220, 53, 69);
    private static final Color GRAY  = new Color(100, 100, 100);

    private final AdminDAO adminDAO = new AdminDAOImpl();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;
    private JButton btnLogin;

    public Login() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Fit Manage - Login Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);
        root.add(buildBrandPanel(), BorderLayout.WEST);
        root.add(buildFormPanel(), BorderLayout.CENTER);

        getRootPane().setDefaultButton(btnLogin);
        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> txtUsername.requestFocusInWindow());
    }

    // =========================================================================
    // PANEL KIRI: BRANDING (UI BARU TEMAN)
    // =========================================================================
    private JPanel buildBrandPanel() {
        BrandPanel brand = new BrandPanel();
        brand.setPreferredSize(new Dimension(380, 520));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(40, 40, 40, 40));

        BrandHeader header = new BrandHeader(BrandHeader.Susunan.ATAS, 130, 38f, 5f);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTagline = new JLabel("Sistem Manajemen Gym");
        lblTagline.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblTagline.setForeground(new Color(200, 220, 235));
        lblTagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><div style='text-align:center; width:240px'>"
                + "Kelola member, trainer, kelas, dan transaksi dalam satu aplikasi.</div></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(170, 195, 215));
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        brand.add(Box.createVerticalGlue());
        brand.add(header);
        brand.add(Box.createVerticalStrut(12));
        brand.add(lblTagline);
        brand.add(Box.createVerticalStrut(26));
        brand.add(lblDesc);
        brand.add(Box.createVerticalGlue());
        return brand;
    }

    // =========================================================================
    // PANEL KANAN: FORM LOGIN
    // =========================================================================
    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(30, 55, 30, 55));
        form.setPreferredSize(new Dimension(500, 520));

        JLabel lblTitle = new JLabel("Selamat Datang");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(NAVY);

        JLabel lblSub = new JLabel("Silakan login sebagai admin untuk melanjutkan.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(GRAY);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(NAVY);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(0, 42));
        txtUsername.putClientProperty("JTextField.placeholderText", "Masukkan username");
        txtUsername.putClientProperty("JComponent.roundRect", true);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(NAVY);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(0, 42));
        txtPassword.putClientProperty("JTextField.placeholderText", "Masukkan password");
        txtPassword.putClientProperty("JComponent.roundRect", true);

        final char defaultEcho = txtPassword.getEchoChar();
        JCheckBox chkShow = new JCheckBox("Tampilkan password");
        chkShow.setOpaque(false);
        chkShow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShow.setForeground(GRAY);
        chkShow.setFocusPainted(false);
        chkShow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chkShow.addActionListener(e ->
                txtPassword.setEchoChar(chkShow.isSelected() ? (char) 0 : defaultEcho));

        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(RED);

        btnLogin = new JButton("Login");
        btnLogin.setBackground(BLUE);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(0, 46));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> doLogin());

        KeyAdapter clearError = new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) { lblError.setText(" "); }
        };
        txtUsername.addKeyListener(clearError);
        txtPassword.addKeyListener(clearError);

        int row = 0;
        addRow(form, lblTitle,    row++, 0);
        addRow(form, lblSub,      row++, 4);
        addRow(form, lblUser,     row++, 32);
        addRow(form, txtUsername, row++, 6);
        addRow(form, lblPass,     row++, 16);
        addRow(form, txtPassword, row++, 6);
        addRow(form, chkShow,     row++, 8);
        addRow(form, lblError,    row++, 10);
        addRow(form, btnLogin,    row++, 12);
        return form;
    }

    private void addRow(JPanel parent, Component comp, int row, int topGap) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(topGap, 0, 0, 0);
        parent.add(comp, c);
    }

    // =========================================================================
    // LOGIKA LOGIN (SUPABASE VIA ADMIN DAO)
    // =========================================================================
    private void doLogin() {
        String username = txtUsername.getText().trim();
        char[] password = txtPassword.getPassword();

        try {
            if (username.isEmpty() || password.length == 0) {
                showError("Username dan password tidak boleh kosong.");
                return;
            }

            if (authenticate(username, password)) {
                openDashboard();
            } else {
                showError("Username atau password salah.");
                txtPassword.setText("");
                txtPassword.requestFocusInWindow();
            }
        } catch (SQLException e) {
            showError("Gagal terhubung ke server database.");
            JOptionPane.showMessageDialog(this,
                    "Tidak dapat memverifikasi login ke Supabase!\nDetail: " + e.getMessage(),
                    "Error Koneksi Database",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private boolean authenticate(String username, char[] password) throws SQLException {
        return adminDAO.cekLogin(username, new String(password));
    }

    private void showError(String message) {
        lblError.setText(message);
    }

    private void openDashboard() {
        dispose();
        new Dashboard().setVisible(true);
    }

    private static class BrandPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, NAVY, getWidth(), getHeight(), new Color(0, 110, 155)));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 255, 255, 18));
            g2.fillOval(-90, -90, 280, 280);
            g2.fillOval(getWidth() - 150, getHeight() - 190, 320, 320);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            System.setProperty("flatlaf.useWindowDecorations", "true");
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("FlatLaf failed to initialize");
        }
        EventQueue.invokeLater(() -> new Login().setVisible(true));
    }
}