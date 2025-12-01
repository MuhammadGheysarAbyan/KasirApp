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
                    selectedCode = getSafeString(model.getValueAt(row, 0));
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
                String shiftValue = rs.getString("shift");
                String shiftDisplay = normalizeShiftDisplay(shiftValue);
                
                model.addRow(new Object[]{
                        rs.getString("user_code"),
                        rs.getString("nama"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("no_telp"),
                        rs.getString("alamat"),
                        shiftDisplay
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load data!\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ----------------- Helper Methods -----------------
    private String getSafeString(Object value) {
        return value == null ? "" : value.toString();
    }

    // Untuk display di tabel (tampilan user friendly)
    private String normalizeShiftDisplay(String shift) {
        if (shift == null || shift.trim().isEmpty()) return "Pagi";
        
        shift = shift.trim().toLowerCase();
        if (shift.contains("24") || shift.contains("admin") || shift.equals("full") || shift.equals("24jam")) {
            return "24 Jam";
        } else if (shift.contains("pagi") || shift.equals("s1") || shift.equals("1") || shift.equals("shift 1")) {
            return "Pagi";
        } else if (shift.contains("siang") || shift.equals("s2") || shift.equals("2") || shift.equals("shift 2")) {
            return "Siang";
        } else if (shift.contains("malam") || shift.equals("s3") || shift.equals("3") || shift.equals("shift 3")) {
            return "Malam";
        } else {
            // Kapitalisasi huruf pertama
            if (!shift.isEmpty()) {
                return shift.substring(0, 1).toUpperCase() + shift.substring(1);
            }
            return shift;
        }
    }

    // Untuk simpan ke database (harus pendek)
    private String normalizeShiftForDatabase(String shift) {
        if (shift == null || shift.trim().isEmpty()) return "Pagi";
        
        shift = shift.trim().toLowerCase();
        if (shift.contains("24") || shift.contains("admin") || shift.contains("jam")) {
            return "24 Jam"; // Simpan sebagai "24 Jam" (6 karakter termasuk spasi)
        } else if (shift.contains("pagi") || shift.equals("s1") || shift.equals("1") || shift.equals("shift 1")) {
            return "Pagi"; // 4 karakter
        } else if (shift.contains("siang") || shift.equals("s2") || shift.equals("2") || shift.equals("shift 2")) {
            return "Siang"; // 5 karakter
        } else if (shift.contains("malam") || shift.equals("s3") || shift.equals("3") || shift.equals("shift 3")) {
            return "Malam"; // 5 karakter
        } else {
            // Potong jika terlalu panjang (maks 10 karakter)
            return shift.length() > 10 ? shift.substring(0, 10) : shift;
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
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

        // Setup combo box shift berdasarkan role
        cbRole.addActionListener(e -> {
            String selectedRole = (String) cbRole.getSelectedItem();
            cbShift.removeAllItems();
            
            if ("admin".equalsIgnoreCase(selectedRole)) {
                // SHIFT KHUSUS ADMIN
                cbShift.addItem("24 Jam");
            } else {
                // SHIFT BIASA UNTUK KASIR
                cbShift.addItem("Pagi");
                cbShift.addItem("Siang");
                cbShift.addItem("Malam");
            }
            
            // Set default value
            if ("admin".equalsIgnoreCase(selectedRole)) {
                cbShift.setSelectedItem("24 Jam");
            } else {
                cbShift.setSelectedItem("Pagi");
            }
        });

        // Trigger initial setup
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
                "", new JLabel("<html><font color='red'>* Wajib diisi</font></html>"),
                "", new JLabel("<html><font color='blue'>Note: Admin memiliki shift 24 Jam</font></html>")
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
                String shiftDisplay = (String) cbShift.getSelectedItem();
                String shiftForDB = normalizeShiftForDatabase(shiftDisplay);

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
                    JOptionPane.showMessageDialog(dialog, "Nomor telepon harus 10-15 digit!");
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
                    ps.setString(9, shiftForDB);

                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        JOptionPane.showMessageDialog(this, "User berhasil ditambahkan!");
                        loadData();
                        dialog.dispose();
                        return;
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error DB: " + e.getMessage());
                    e.printStackTrace();
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
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih user dulu!");
            return;
        }

        // Ambil data dengan penanganan null
        String kodeUserAwal = getSafeString(model.getValueAt(row, 0));
        String namaAwal = getSafeString(model.getValueAt(row, 1));
        String usernameAwal = getSafeString(model.getValueAt(row, 2));
        String passwordAwal = getSafeString(model.getValueAt(row, 3));
        String roleAwal = getSafeString(model.getValueAt(row, 4));
        String emailAwal = getSafeString(model.getValueAt(row, 5));
        String noTelpAwal = getSafeString(model.getValueAt(row, 6));
        String alamatAwal = getSafeString(model.getValueAt(row, 7));
        String shiftDisplayAwal = getSafeString(model.getValueAt(row, 8));

        // Validasi data penting
        if (kodeUserAwal.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data user tidak valid!");
            return;
        }

        JTextField tfNama = new JTextField(namaAwal);
        JTextField tfUsername = new JTextField(usernameAwal);
        JTextField tfPassword = new JTextField(passwordAwal);
        JTextField tfEmail = new JTextField(emailAwal);
        JTextField tfNoTelp = new JTextField(noTelpAwal);
        JTextField tfAlamat = new JTextField(alamatAwal);

        String[] roles = {"admin", "kasir"};
        JComboBox<String> cbRole = new JComboBox<>(roles);
        cbRole.setSelectedItem(roleAwal.isEmpty() ? "kasir" : roleAwal);
        
        JComboBox<String> cbShift = new JComboBox<>();

        // Setup shift berdasarkan role
        cbRole.addActionListener(e -> {
            String selectedRole = (String) cbRole.getSelectedItem();
            cbShift.removeAllItems();
            
            if ("admin".equalsIgnoreCase(selectedRole)) {
                // SHIFT KHUSUS ADMIN
                cbShift.addItem("24 Jam");
            } else {
                // SHIFT BIASA UNTUK KASIR
                cbShift.addItem("Pagi");
                cbShift.addItem("Siang");
                cbShift.addItem("Malam");
            }
            
            // Set shift yang sesuai dengan data awal
            if ("admin".equalsIgnoreCase(selectedRole)) {
                cbShift.setSelectedItem("24 Jam");
            } else {
                // Jika sebelumnya admin dan diganti ke kasir, set default Pagi
                if ("admin".equalsIgnoreCase(roleAwal) && "kasir".equalsIgnoreCase(selectedRole)) {
                    cbShift.setSelectedItem("Pagi");
                } else {
                    // Pertahankan shift yang ada jika tersedia
                    String normalizedShift = normalizeShiftForDatabase(shiftDisplayAwal);
                    if (cbShift.getItemCount() > 0) {
                        boolean shiftExists = false;
                        for (int i = 0; i < cbShift.getItemCount(); i++) {
                            if (cbShift.getItemAt(i).equalsIgnoreCase(normalizedShift)) {
                                shiftExists = true;
                                break;
                            }
                        }
                        if (shiftExists) {
                            cbShift.setSelectedItem(normalizedShift);
                        } else {
                            cbShift.setSelectedIndex(0);
                        }
                    }
                }
            }
        });

        // Trigger initial setup
        cbRole.getActionListeners()[0].actionPerformed(null);

        Object[] message = {
                "Kode User:", new JLabel(kodeUserAwal),
                "Nama Lengkap:*", tfNama,
                "Username:*", tfUsername,
                "Password:*", tfPassword,
                "Role:*", cbRole,
                "Email:*", tfEmail,
                "No. Telp:*", tfNoTelp,
                "Alamat:*", tfAlamat,
                "Shift:*", cbShift,
                "", new JLabel("<html><font color='red'>* Wajib diisi</font></html>"),
                "", new JLabel("<html><font color='blue'>Note: Admin memiliki shift 24 Jam</font></html>")
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit User - " + kodeUserAwal, 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            // Validasi input
            if (isFieldEmpty(tfNama.getText().trim(), tfUsername.getText().trim(), 
                            tfPassword.getText().trim(), tfEmail.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Field wajib tidak boleh kosong!");
                return;
            }

            if (!isValidEmail(tfEmail.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Format email tidak valid!");
                return;
            }

            if (!isValidPhoneNumber(tfNoTelp.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Nomor telepon harus 10-15 digit!");
                return;
            }

            try (Connection conn = Database.getConnection()) {
                // Cek username duplikat (kecuali username sendiri)
                String checkSql = "SELECT COUNT(*) FROM users WHERE username = ? AND user_code != ?";
                PreparedStatement checkPs = conn.prepareStatement(checkSql);
                checkPs.setString(1, tfUsername.getText().trim());
                checkPs.setString(2, kodeUserAwal);
                ResultSet rs = checkPs.executeQuery();
                
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Username sudah digunakan user lain!");
                    return;
                }

                String shiftDisplay = (String) cbShift.getSelectedItem();
                String shiftForDB = normalizeShiftForDatabase(shiftDisplay);

                System.out.println("Debug - Shift untuk database: '" + shiftForDB + "' (length: " + shiftForDB.length() + ")");

                // Pastikan kolom shift cukup panjang (minimal VARCHAR(10))
                String sql = "UPDATE users SET nama=?, username=?, password=?, role=?, email=?, no_telp=?, alamat=?, shift=? WHERE user_code=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, tfNama.getText().trim());
                ps.setString(2, tfUsername.getText().trim());
                ps.setString(3, tfPassword.getText().trim());
                ps.setString(4, (String) cbRole.getSelectedItem());
                ps.setString(5, tfEmail.getText().trim());
                ps.setString(6, tfNoTelp.getText().trim());
                ps.setString(7, tfAlamat.getText().trim());
                ps.setString(8, shiftForDB);
                ps.setString(9, kodeUserAwal);

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Data user berhasil diperbarui!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal update data user!");
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("Data truncated")) {
                    // Jika masih error, ubah ke nilai yang lebih pendek
                    JOptionPane.showMessageDialog(this, 
                        "Error: Kolom shift terlalu pendek di database.\n" +
                        "Perbaiki struktur tabel dengan:\n" +
                        "ALTER TABLE users MODIFY shift VARCHAR(20);");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
                e.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ----------------- hapusUser() -----------------
    private void hapusUser() {
        if (selectedCode == null) {
            JOptionPane.showMessageDialog(this, "Pilih user dulu!");
            return;
        }
        
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih user dulu!");
            return;
        }
        
        String userName = getSafeString(model.getValueAt(row, 1));
        String userCode = getSafeString(model.getValueAt(row, 0));
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Hapus user: " + userName + " (" + userCode + ")?", 
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Database.getConnection()) {
            String sql = "DELETE FROM users WHERE user_code=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, selectedCode);
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                JOptionPane.showMessageDialog(this, "User berhasil dihapus!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal hapus user!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus user!\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManajemenUser("admin").setVisible(true));
    }
}