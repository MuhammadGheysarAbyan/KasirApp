import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.*;

public class ManajemenUser extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private String selectedCode = null;
    private String usernameAdmin;

    public ManajemenUser(String usernameAdmin) {
        this.usernameAdmin = usernameAdmin;
        setTitle("👤 Manajemen User - " + usernameAdmin);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ================= PANEL UTAMA DENGAN GRADIENT =================
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 102, 204),
                        0, getHeight(), Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // ================= HEADER =================
        ImageIcon iconHeader = null;
        try {
            iconHeader = new ImageIcon(getClass().getResource("/img/user.png"));
            Image img = iconHeader.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            iconHeader = new ImageIcon(img);
        } catch (Exception e) {
            System.out.println("Gagal load gambar header");
        }

        JLabel lblHeader;
        if (iconHeader != null) {
            lblHeader = new JLabel("Manajemen User", iconHeader, JLabel.CENTER);
            lblHeader.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblHeader.setIconTextGap(10);
        } else {
            lblHeader = new JLabel("Manajemen User", JLabel.CENTER);
        }
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // ================= PANEL TABEL =================
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(new LineBorder(new Color(52, 152, 219), 2, true));
        tablePanel.setBackground(new Color(255, 255, 255, 230));

        model = new DefaultTableModel(
                new String[]{"ID", "Nama", "Username", "Password", "Role", "Email", "No. Telp", "Alamat", "Shift"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.setSelectionBackground(new Color(52, 152, 219, 120));
        table.setSelectionForeground(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // ================= PANEL BUTTON =================
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBtn.setOpaque(false);
        JButton btnTambah = createButton("Tambah", "/img/add.png", new Color(52, 152, 219));
        JButton btnEdit   = createButton("Edit", "/img/edit.png", new Color(46, 204, 113));
        JButton btnHapus  = createButton("Hapus", "/img/delete.png", new Color(231, 76, 60));
        JButton btnBack   = createButton("Kembali", "/img/back.png", new Color(127, 140, 141));
        panelBtn.add(btnTambah);
        panelBtn.add(btnEdit);
        panelBtn.add(btnHapus);
        panelBtn.add(btnBack);
        mainPanel.add(panelBtn, BorderLayout.SOUTH);

        add(mainPanel);
        loadData();

        TableColumnModel columnModel = table.getColumnModel();
        try {
            columnModel.getColumn(0).setPreferredWidth(90);
            columnModel.getColumn(1).setPreferredWidth(180);
            columnModel.getColumn(2).setPreferredWidth(140);
            columnModel.getColumn(3).setPreferredWidth(120);
            columnModel.getColumn(4).setPreferredWidth(80);
            columnModel.getColumn(5).setPreferredWidth(200);
            columnModel.getColumn(6).setPreferredWidth(120);
            columnModel.getColumn(7).setPreferredWidth(250);
            columnModel.getColumn(8).setPreferredWidth(90);
        } catch (Exception ignore) {}

        // ================= ACTION =================
        btnTambah.addActionListener(e -> tambahUser());
        btnEdit.addActionListener(e -> editUser());
        btnHapus.addActionListener(e -> hapusUser());
        btnBack.addActionListener(e -> {
            new DashboardAdmin(usernameAdmin).setVisible(true);
            dispose();
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                try {
                    selectedCode = (String) model.getValueAt(row, 0);
                } catch (Exception ex) {
                    selectedCode = null;
                }
            }
        });
    }

    private JButton createButton(String text, String iconPath, Color bgColor) {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        } catch (Exception e) {}

        JButton btn = icon != null ? new JButton(text, icon) : new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(10);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    private void loadData() {
        model.setRowCount(0);
        selectedCode = null;
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT user_code, nama, username, password, role, email, no_telp, alamat, shift FROM users ORDER BY id DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("user_code"),
                        rs.getString("nama"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("no_telp"),
                        rs.getString("alamat"),
                        rs.getString("shift")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load data!\n" + e.getMessage());
        }
    }

    // ----------------- Helper Validasi -----------------
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        return phone.matches("\\d{10,15}");
    }

    private boolean isFieldEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) return true;
        }
        return false;
    }

    private String generateUserCode(Connection conn) throws SQLException {
        String newCode = "USR001";
        String sqlCode = "SELECT user_code FROM users WHERE user_code LIKE 'USR%' ORDER BY user_code DESC LIMIT 1";
        try (PreparedStatement psCode = conn.prepareStatement(sqlCode);
             ResultSet rsCode = psCode.executeQuery()) {
            if (rsCode.next()) {
                String lastCode = rsCode.getString("user_code");
                try {
                    int num = Integer.parseInt(lastCode.substring(3));
                    num++;
                    newCode = String.format("USR%03d", num);
                } catch (Exception ignored) {}
            }
        }
        return newCode;
    }

    // ----------------- tambahUser() -----------------
    private void tambahUser() {
        JTextField tfNama = new JTextField();
        JTextField tfUsername = new JTextField();
        JTextField tfPassword = new JTextField();
        JTextField tfEmail = new JTextField();
        JTextField tfNoTelp = new JTextField();
        JTextField tfAlamat = new JTextField();

        String[] roles = {"admin", "kasir"};
        JComboBox<String> cbRole = new JComboBox<>(roles);
        JComboBox<String> cbShift = new JComboBox<>();

        cbRole.addActionListener(e -> {
            String selectedRole = (String) cbRole.getSelectedItem();
            cbShift.removeAllItems();
            if ("admin".equalsIgnoreCase(selectedRole)) {
                cbShift.addItem("Shift 3");
            } else {
                cbShift.addItem("Shift 1");
                cbShift.addItem("Shift 2");
            }
        });
        cbRole.setSelectedIndex(0);
        cbRole.getActionListeners()[0].actionPerformed(null);

        Object[] message = {
                "Nama Lengkap:*", tfNama,
                "Username:*", tfUsername,
                "Password:*", tfPassword,
                "Role:*", cbRole,
                "Email:*", tfEmail,
                "No. Telp:*", tfNoTelp,
                "Alamat:*", tfAlamat,
                "Shift:*", cbShift,
                "", new JLabel("<html><font color='red'>* Wajib diisi</font></html>")
        };

        JOptionPane optionPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(this, "Tambah User Baru");
        dialog.setModal(true);

        while (true) {
            dialog.setVisible(true);
            Object result = optionPane.getValue();

            if (result == null || result.equals(JOptionPane.UNINITIALIZED_VALUE)
                    || result.equals(JOptionPane.CANCEL_OPTION)
                    || result.equals(JOptionPane.CLOSED_OPTION)) {
                return;
            }

            if (result.equals(JOptionPane.OK_OPTION)) {
                String nama = tfNama.getText().trim();
                String username = tfUsername.getText().trim();
                String password = tfPassword.getText().trim();
                String email = tfEmail.getText().trim();
                String noTelp = tfNoTelp.getText().trim();
                String alamat = tfAlamat.getText().trim();
                String role = (String) cbRole.getSelectedItem();
                String shift = (String) cbShift.getSelectedItem();

                if (isFieldEmpty(nama, username, password, email, noTelp, alamat)) {
                    JOptionPane.showMessageDialog(dialog, "Semua field wajib diisi!");
                    optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    continue;
                }

                if (!isValidEmail(email)) {
                    JOptionPane.showMessageDialog(dialog, "Format email tidak valid!");
                    optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    continue;
                }

                if (!isValidPhoneNumber(noTelp)) {
                    JOptionPane.showMessageDialog(dialog, "Nomor telepon tidak valid!");
                    optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    continue;
                }

                try (Connection conn = Database.getConnection()) {
                    String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
                    PreparedStatement checkPs = conn.prepareStatement(checkSql);
                    checkPs.setString(1, username);
                    ResultSet rs = checkPs.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(dialog, "Username sudah digunakan!");
                        optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                        continue;
                    }

                    String newCode = generateUserCode(conn);
                    String sql = "INSERT INTO users (user_code, nama, username, password, role, email, no_telp, alamat, shift) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, newCode);
                    ps.setString(2, nama);
                    ps.setString(3, username);
                    ps.setString(4, password);
                    ps.setString(5, role);
                    ps.setString(6, email);
                    ps.setString(7, noTelp);
                    ps.setString(8, alamat);
                    ps.setString(9, shift);

                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        JOptionPane.showMessageDialog(this, "User berhasil ditambahkan!");
                        loadData();
                        dialog.dispose();
                        return;
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error DB: " + e.getMessage());
                }
            }
        }
    }

    // ----------------- editUser() -----------------
    private void editUser() {
        if (selectedCode == null) {
            JOptionPane.showMessageDialog(this, "Pilih user dulu!");
            return;
        }

        int row = table.getSelectedRow();
        String kodeUserAwal = model.getValueAt(row, 0).toString();
        String namaAwal = model.getValueAt(row, 1).toString();
        String usernameAwal = model.getValueAt(row, 2).toString();
        String passwordAwal = model.getValueAt(row, 3).toString();
        String roleAwal = model.getValueAt(row, 4).toString();
        String emailAwal = model.getValueAt(row, 5).toString();
        String noTelpAwal = model.getValueAt(row, 6).toString();
        String alamatAwal = model.getValueAt(row, 7).toString();
        String shiftAwal = model.getValueAt(row, 8).toString();

        JTextField tfNama = new JTextField(namaAwal);
        JTextField tfUsername = new JTextField(usernameAwal);
        JTextField tfPassword = new JTextField(passwordAwal);
        JTextField tfEmail = new JTextField(emailAwal);
        JTextField tfNoTelp = new JTextField(noTelpAwal);
        JTextField tfAlamat = new JTextField(alamatAwal);

        JComboBox<String> cbRole = new JComboBox<>(new String[]{roleAwal});
        JComboBox<String> cbShift = new JComboBox<>();

        if ("admin".equalsIgnoreCase(roleAwal)) {
            cbShift.addItem("Shift 3");
        } else {
            cbShift.addItem("Shift 1");
            cbShift.addItem("Shift 2");
        }
        cbShift.setSelectedItem(shiftAwal);

        Object[] message = {
                "Kode User:", new JLabel(kodeUserAwal),
                "Nama Lengkap:*", tfNama,
                "Username:*", tfUsername,
                "Password:*", tfPassword,
                "Role:*", cbRole,
                "Email:*", tfEmail,
                "No. Telp:*", tfNoTelp,
                "Alamat:*", tfAlamat,
                "Shift:*", cbShift
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = Database.getConnection()) {
                String sql = "UPDATE users SET nama=?, username=?, password=?, email=?, no_telp=?, alamat=?, shift=? WHERE user_code=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, tfNama.getText().trim());
                ps.setString(2, tfUsername.getText().trim());
                ps.setString(3, tfPassword.getText().trim());
                ps.setString(4, tfEmail.getText().trim());
                ps.setString(5, tfNoTelp.getText().trim());
                ps.setString(6, tfAlamat.getText().trim());
                ps.setString(7, (String) cbShift.getSelectedItem());
                ps.setString(8, kodeUserAwal);

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Data user berhasil diperbarui!");
                    loadData();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void hapusUser() {
        if (selectedCode == null) {
            JOptionPane.showMessageDialog(this, "Pilih user dulu!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus user " + selectedCode + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Database.getConnection()) {
            String sql = "DELETE FROM users WHERE user_code=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, selectedCode);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "User berhasil dihapus!");
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus user!\n" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManajemenUser("admin").setVisible(true));
    }
}
