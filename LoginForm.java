import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class LoginForm extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JCheckBox showPass;
    private JComboBox<String> cmbRole;

    public LoginForm() {
        setTitle("🔑 Kasir App - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);                               
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // ================= BACKGROUND GRADIENT =================
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color color1 = new Color(0, 102, 204);
                Color color2 = Color.WHITE;
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        background.setLayout(new GridBagLayout());
        add(background, BorderLayout.CENTER);

        // ================= CARD LOGIN =================
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(450, 500));
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 0, 2, 0); // 🔥 lebih rapat
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // ================= LOGO =================
        JLabel lblLogo;
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/img/logo_bg.png"));
            Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            lblLogo = new JLabel(new ImageIcon(img), JLabel.CENTER);
        } catch (Exception e) {
            lblLogo = new JLabel("🛒", JLabel.CENTER);
            lblLogo.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        }
        card.add(lblLogo, gbc);

        // ================= APP TITLE =================
        gbc.gridy++;
        JLabel lblTitle = new JLabel("KASIR APP", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));
        card.add(lblTitle, gbc);

        // ================= USERNAME LABEL =================
        gbc.gridy++;
        JLabel lblUserLabel = new JLabel("Username");
        lblUserLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUserLabel.setForeground(new Color(80, 80, 80));
        lblUserLabel.setBorder(null);
        card.add(lblUserLabel, gbc);

        // ================= USERNAME FIELD =================
        gbc.gridy++;
        txtUser = new JTextField();
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUser.setPreferredSize(new Dimension(0, 30));
        txtUser.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)));
        card.add(txtUser, gbc);

        // ================= PASSWORD LABEL =================
        gbc.gridy++;
        JLabel lblPassLabel = new JLabel("Password");
        lblPassLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPassLabel.setForeground(new Color(80, 80, 80));
        lblPassLabel.setBorder(null);
        card.add(lblPassLabel, gbc);

        // ================= PASSWORD FIELD =================
        gbc.gridy++;
        txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPass.setPreferredSize(new Dimension(0, 30));
        txtPass.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)));
        card.add(txtPass, gbc);

        // ================= ROLE LABEL =================
        gbc.gridy++;
        JLabel lblRole = new JLabel("Login Sebagai");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRole.setForeground(new Color(80, 80, 80));
        lblRole.setBorder(null);
        card.add(lblRole, gbc);

        // ================= ROLE DROPDOWN =================
        gbc.gridy++;
        cmbRole = new JComboBox<>(new String[]{"admin", "kasir"});
        cmbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(cmbRole, gbc);

        // ================= SHOW PASSWORD =================
        gbc.gridy++;
        showPass = new JCheckBox("Show Password");
        showPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPass.setOpaque(false);
        showPass.addActionListener(e -> txtPass.setEchoChar(showPass.isSelected() ? (char)0 : '•'));
        card.add(showPass, gbc);

        // ================= LOGIN BUTTON =================
        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 2, 0); // tombol sedikit diberi ruang
        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(0, 102, 204));
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(0, 35));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> login());
        card.add(btnLogin, gbc);

        // Masukin card ke background
        background.add(card);
    }

    private void login() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword()).trim();
        String roleDipilih = (String) cmbRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username dan Password tidak boleh kosong!",
                    "Login Gagal",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String roleDb = rs.getString("role");
                int userId = rs.getInt("id");
                String shift = rs.getString("shift");

                // Cek apakah role di database sama dengan yang dipilih
                if (!roleDb.equalsIgnoreCase(roleDipilih)) {
                    JOptionPane.showMessageDialog(this,
                            "Role tidak sesuai dengan akun!\nAkun ini terdaftar sebagai: " + roleDb);
                    return;
                }

                // === MODIFIKASI: Admin bebas login tanpa pengecekan shift ===
                if (roleDb.equalsIgnoreCase("admin")) {
                    // Admin bisa login kapan saja tanpa pengecekan shift
                    // Tidak ada pengecekan shift untuk admin
                } else if (roleDb.equalsIgnoreCase("kasir")) {
                    // Kasir tetap dicek shift-nya sesuai jadwal
                    if (!cekShift(userId)) {
                        JOptionPane.showMessageDialog(this,
                                "Kamu tidak memiliki jadwal shift hari ini.\nCoba login sesuai jadwal shift kamu.");
                        return;
                    }
                }

                JOptionPane.showMessageDialog(this, "Login berhasil sebagai " + roleDb);
                txtUser.setText("");
                txtPass.setText("");

                if (roleDb.equalsIgnoreCase("admin")) {
                    new DashboardAdmin(username).setVisible(true);
                } else {
                    new DashboardKasir(username).setVisible(true);
                }

                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Username atau password salah!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error koneksi database!");
        }
    }

    private boolean cekShift(int userId) {
        int hari = LocalDate.now().getDayOfMonth(); 
        boolean idGanjil = userId % 2 != 0;
        boolean tglGanjil = hari % 2 != 0;
        return (idGanjil && tglGanjil) || (!idGanjil && !tglGanjil);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}