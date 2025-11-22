import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ManajemenProduk extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private Integer selectedId = null;
    private String selectedFoto = null;
    private String admin;
    private String shift = "Shift 1"; // default shift
    private DecimalFormat df;

    // ================= FORMAT RUPIAH =================
    private void formatRupiah(JTextField field) {
        field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String text = field.getText().replaceAll("[^0-9]", "");
                if (text.isEmpty()) {
                    field.setText("");
                    return;
                }
                try {
                    long value = Long.parseLong(text);
                    field.setText(df.format(value));
                } catch (Exception ex) { 
                    field.setText("");
                }
            }
        });
    }

    private long parseRupiah(String text) {
        if (text == null || text.isEmpty()) return 0;
        text = text.replaceAll("[^0-9]", "");
        if (text.isEmpty()) return 0;
        return Long.parseLong(text);
    }

    public ManajemenProduk() {
        this("Guest");
    }

    public ManajemenProduk(String admin) {
        this.admin = admin;

        // Setup format angka
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        df = new DecimalFormat("#,###", symbols);

        setTitle("📦 Manajemen Produk - " + admin);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 102, 204), 0, getHeight(), Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Header
        JLabel lblHeader = createHeaderLabel("Manajemen Produk", "/img/box.png");
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // ================= TABEL PRODUK =================
        model = new DefaultTableModel(
            new String[]{"ID", "Kode Produk", "Nama Produk", "Kategori", "Harga", "Stok", "Foto"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) return ImageIcon.class; // foto
                return Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(60);
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);
        
        // Sembunyikan kolom ID
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2, true));

        // ================= SEARCH FIELD =================
        JTextField tfSearch = new JTextField();
        tfSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tfSearch.setPreferredSize(new Dimension(180, 35));
        tfSearch.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2, true));

        // ================= ICON SEARCH PUTIH TANPA GAMBAR =================
        Icon whiteSearchIcon = new Icon() {
            private final int size = 14;
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.drawOval(x, y, size - 4, size - 4);
                g2.drawLine(x + size - 6, y + size - 6, x + size, y + size);
                g2.dispose();
            }
            public int getIconWidth() { return size; }
            public int getIconHeight() { return size; }
        };

        // ================= BUTTON CARI =================
        JButton btnCari = new JButton("Cari", whiteSearchIcon);
        btnCari.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCari.setBackground(new Color(0, 153, 255));
        btnCari.setForeground(Color.WHITE);
        btnCari.setFocusPainted(false);
        btnCari.setBorderPainted(false);
        btnCari.setOpaque(true);
        btnCari.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCari.setPreferredSize(new Dimension(90, 32));
        btnCari.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnCari.setIconTextGap(6);

        btnCari.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnCari.setBackground(new Color(0, 120, 210)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btnCari.setBackground(new Color(0, 153, 255)); }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setOpaque(false);
        topPanel.add(tfSearch);
        topPanel.add(btnCari);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setOpaque(false);
        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(sp, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ================= FILTER REALTIME =================
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        Runnable filterAction = () -> {
            String text = tfSearch.getText().trim();
            if (text.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2, 3)); // Filter by kode, nama, kategori
            }
        };

        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterAction.run(); }
            public void removeUpdate(DocumentEvent e) { filterAction.run(); }
            public void changedUpdate(DocumentEvent e) { filterAction.run(); }
        });

        btnCari.addActionListener(e -> filterAction.run());

        // ================= PANEL TOMBOL =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);

        JButton btnTambah = createButton("Tambah", "/img/add.png", new Color(0, 153, 255));
        JButton btnEdit   = createButton("Edit", "/img/edit.png", new Color(0, 204, 102));
        JButton btnHapus  = createButton("Hapus", "/img/delete.png", new Color(255, 77, 77));
        JButton btnBack   = createButton("Kembali", "/img/back.png", new Color(153, 153, 153));
        JButton btnKelolaKategori = createButton("Kategori", "/img/category.png", new Color(155, 89, 182));

        btnPanel.add(btnTambah);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnKelolaKategori);
        btnPanel.add(btnBack);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // ================= ACTIONS =================
        loadData();

        btnTambah.addActionListener(e -> {
            table.clearSelection();
            selectedId = null;
            tambahProduk();
        });
        btnEdit.addActionListener(e -> editProduk());
        btnHapus.addActionListener(e -> hapusProduk());
        btnBack.addActionListener(e -> {
            DashboardAdmin dash = new DashboardAdmin(admin);
            dash.setVisible(true);
            dispose();
        });
        btnKelolaKategori.addActionListener(e -> new KelolaKategoriDialog(this).setVisible(true));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    int modelRow = table.convertRowIndexToModel(row);
                    selectedId = (Integer) model.getValueAt(modelRow, 0); // ID ada di kolom 0
                    selectedFoto = null;
                }
            }
        });
    }

    // ================= HEADER =================
    private JLabel createHeaderLabel(String text, String iconPath) {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        } catch (Exception e) {
            System.out.println("Gagal load gambar header: " + iconPath);
        }

        JLabel lblHeader;
        if (icon != null) {
            lblHeader = new JLabel(text, icon, JLabel.CENTER);
            lblHeader.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblHeader.setIconTextGap(10);
        } else {
            lblHeader = new JLabel(text, JLabel.CENTER);
        }
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        return lblHeader;
    }

    private void loadData() {
        model.setRowCount(0);
        selectedId = null;
        try (Connection conn = Database.getConnection()) {
            // Query yang sesuai dengan struktur database
            String sql = "SELECT p.id, p.kode, p.nama_produk, k.nama_kategori, p.harga, p.stok, p.foto " +
                        "FROM produk p " +
                        "LEFT JOIN kategori k ON p.kategori_id = k.id " +
                        "ORDER BY k.nama_kategori ASC, p.id ASC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String kode = rs.getString("kode");
                if (kode == null || kode.trim().isEmpty()) {
                    kode = "-";
                }
                String kategori = rs.getString("nama_kategori");
                if (kategori == null) {
                    kategori = "Tanpa Kategori";
                }
                
                String fotoPath = rs.getString("foto");
                ImageIcon imgIcon = null;
                if (fotoPath != null && !fotoPath.isEmpty()) {
                    try {
                        Image img = new ImageIcon(fotoPath).getImage()
                                .getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                        imgIcon = new ImageIcon(img);
                    } catch (Exception ex) {
                        System.out.println("Gagal load foto: " + fotoPath);
                    }
                }

                model.addRow(new Object[]{
                    id,                 // kolom 0 - ID
                    kode,               // kolom 1 - Kode Produk
                    rs.getString("nama_produk"), // kolom 2 - Nama Produk
                    kategori,           // kolom 3 - Kategori
                    "Rp " + df.format(rs.getLong("harga")), // kolom 4 - Harga
                    rs.getInt("stok"),  // kolom 5 - Stok
                    imgIcon             // kolom 6 - Foto
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal load data!\n" + e.getMessage());
        }
    }

    private String generateKodeProduk(Connection conn) throws SQLException {
        // Generate kode format: PRO-001, PRO-002, dst
        String sql = "SELECT kode FROM produk ORDER BY id DESC LIMIT 1";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        int nextNum = 1;
        if (rs.next()) {
            String lastKode = rs.getString("kode");
            if (lastKode != null && lastKode.startsWith("PRO-")) {
                try {
                    String numberPart = lastKode.substring(4);
                    nextNum = Integer.parseInt(numberPart) + 1;
                } catch (NumberFormatException e) {
                    // Jika format tidak sesuai, lanjutkan dengan angka berikutnya
                }
            }
        }
        return String.format("PRO-%03d", nextNum);
    }

    private void tambahProduk() {
        JTextField tfNama = new JTextField();
        JTextField tfHarga = new JTextField();
        JTextField tfStok = new JTextField();
        formatRupiah(tfHarga);

        JComboBox<String> cbKategori = new JComboBox<>();
        loadKategoriFromDatabase(cbKategori);

        JButton btnPilihFoto = new JButton("Pilih Foto");
        JLabel lblFoto = new JLabel("");
        final String[] pathFoto = {null};

        btnPilihFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                pathFoto[0] = file.getAbsolutePath();
                lblFoto.setText(file.getName());
            }
        });

        Object[] message = {
            "Nama Produk:", tfNama,
            "Kategori:", cbKategori,
            "Harga:", tfHarga,
            "Stok:", tfStok,
            "Foto:", btnPilihFoto, lblFoto
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Tambah Produk", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if (tfNama.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama produk harus diisi!");
                return;
            }

            try (Connection conn = Database.getConnection()) {
                // Generate kode produk
                String kodeBaru = generateKodeProduk(conn);
                
                // Get kategori_id dari kategori yang dipilih
                String kategoriNama = cbKategori.getSelectedItem().toString();
                int kategoriId = getKategoriIdByName(conn, kategoriNama);

                long harga = parseRupiah(tfHarga.getText());
                int stok = Integer.parseInt(tfStok.getText());

                // Insert ke database
                String sql = "INSERT INTO produk (kode, nama_produk, kategori_id, harga, stok, foto) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setString(1, kodeBaru);
                pst.setString(2, tfNama.getText().trim());
                pst.setInt(3, kategoriId);
                pst.setLong(4, harga);
                pst.setInt(5, stok);
                pst.setString(6, pathFoto[0]);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan!\nKode: " + kodeBaru);
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal tambah produk!\n" + e.getMessage());
            }
        }
    }

    private void editProduk() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Pilih produk dulu!");
            return;
        }

        int row = table.getSelectedRow(); 
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk dulu!");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        String namaAwal = model.getValueAt(modelRow, 2).toString();
        String kategoriAwal = model.getValueAt(modelRow, 3).toString();
        String hargaAwal = model.getValueAt(modelRow, 4).toString().replace("Rp ", "").replaceAll("[^0-9]", "");
        String stokAwal = model.getValueAt(modelRow, 5).toString();

        JTextField tfNama = new JTextField(namaAwal);
        JTextField tfHarga = new JTextField(hargaAwal);
        JTextField tfStok = new JTextField(stokAwal);
        formatRupiah(tfHarga);

        JComboBox<String> cbKategori = new JComboBox<>();
        loadKategoriFromDatabase(cbKategori);
        cbKategori.setSelectedItem(kategoriAwal);

        JButton btnPilihFoto = new JButton("Pilih Foto");
        JLabel lblFoto = new JLabel("");
        final String[] pathFoto = {null};

        btnPilihFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                pathFoto[0] = file.getAbsolutePath();
                lblFoto.setText(file.getName());
            }
        });

        Object[] message = {
            "Nama Produk:", tfNama,
            "Kategori:", cbKategori,
            "Harga:", tfHarga,
            "Stok:", tfStok,
            "Foto:", btnPilihFoto, lblFoto
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit Produk", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if (tfNama.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama produk harus diisi!");
                return;
            }

            try (Connection conn = Database.getConnection()) {
                long harga = parseRupiah(tfHarga.getText());
                int stok = Integer.parseInt(tfStok.getText());
                
                // Get kategori_id dari kategori yang dipilih
                String kategoriNama = cbKategori.getSelectedItem().toString();
                int kategoriId = getKategoriIdByName(conn, kategoriNama);

                String sql = "UPDATE produk SET nama_produk=?, kategori_id=?, harga=?, stok=?, foto=COALESCE(?, foto) WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, tfNama.getText().trim());
                pst.setInt(2, kategoriId);
                pst.setLong(3, harga);
                pst.setInt(4, stok);
                pst.setString(5, pathFoto[0]);
                pst.setInt(6, selectedId);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Produk berhasil diupdate!");
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal update produk!\n" + e.getMessage());
            }
        }
    }

    private void hapusProduk() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Pilih produk dulu!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus produk ini?", 
            "Konfirmasi Hapus", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Database.getConnection()) {
            // Cek apakah produk pernah digunakan di transaksi
            String checkSql = "SELECT COUNT(*) FROM detail_transaksi WHERE produk_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, selectedId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                    "❌ Produk ini sudah pernah digunakan dalam transaksi.\nTidak dapat dihapus untuk menjaga integritas data.",
                    "Produk Terpakai",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Hapus produk
            String sql = "DELETE FROM produk WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, selectedId);
            int affectedRows = pst.executeUpdate();
            
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Produk berhasil dihapus!");
                loadData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal hapus produk!\n" + e.getMessage());
        }
    }

    private void loadKategoriFromDatabase(JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, nama_kategori FROM kategori ORDER BY nama_kategori ASC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                combo.addItem(rs.getString("nama_kategori"));
            }
            // Tambah default jika tidak ada kategori
            if (combo.getItemCount() == 0) {
                combo.addItem("Umum");
            }
        } catch (Exception e) {
            e.printStackTrace();
            combo.addItem("Umum");
        }
    }

    private int getKategoriIdByName(Connection conn, String kategoriName) throws SQLException {
        String sql = "SELECT id FROM kategori WHERE nama_kategori = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, kategoriName);
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            // Jika kategori tidak ditemukan, buat kategori baru
            String insertSql = "INSERT INTO kategori (nama_kategori) VALUES (?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, kategoriName);
            insertStmt.executeUpdate();
            
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        return 1; // Default ke kategori pertama
    }

    private JButton createButton(String text, String iconPath, Color bgColor) {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        } catch (Exception e) {
            System.out.println("Gagal load icon: " + iconPath);
        }

        JButton btn = new JButton(text, icon);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManajemenProduk("Admin").setVisible(true));
    }
}