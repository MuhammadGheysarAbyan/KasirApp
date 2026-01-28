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
    private String shift = "Shift 1";
    private DecimalFormat df;
    private JTextField tfSearch;
    private TableRowSorter<DefaultTableModel> sorter;

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
// ================= PANEL SEARCH =================
JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // UBAH KE LEFT
searchPanel.setOpaque(false);

tfSearch = new JTextField();
tfSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
tfSearch.setPreferredSize(new Dimension(300, 35));
tfSearch.setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
    BorderFactory.createEmptyBorder(5, 10, 5, 10)
));
tfSearch.setToolTipText("Cari berdasarkan kode, nama produk, atau kategori...");

// Icon Search
Icon whiteSearchIcon = new Icon() {
    private final int size = 16;
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x, y, size - 6, size - 6);
        g2.drawLine(x + size - 6, y + size - 6, x + size, y + size);
        g2.dispose();
    }
    public int getIconWidth() { return size; }
    public int getIconHeight() { return size; }
};

// Button Cari
JButton btnCari = new JButton("Cari", whiteSearchIcon);
btnCari.setFont(new Font("Segoe UI", Font.BOLD, 13));
btnCari.setBackground(new Color(0, 153, 255));
btnCari.setForeground(Color.WHITE);
btnCari.setFocusPainted(false);
btnCari.setBorderPainted(false);
btnCari.setOpaque(true);
btnCari.setCursor(new Cursor(Cursor.HAND_CURSOR));
btnCari.setPreferredSize(new Dimension(100, 35));
btnCari.setHorizontalTextPosition(SwingConstants.RIGHT);
btnCari.setIconTextGap(8);

btnCari.addMouseListener(new java.awt.event.MouseAdapter() {
    public void mouseEntered(java.awt.event.MouseEvent evt) { 
        btnCari.setBackground(new Color(0, 120, 210)); 
    }
    public void mouseExited(java.awt.event.MouseEvent evt) { 
        btnCari.setBackground(new Color(0, 153, 255)); 
    }
});

searchPanel.add(tfSearch);
searchPanel.add(btnCari);

        // ================= TABEL PRODUK =================
        model = new DefaultTableModel(
            new String[]{"ID", "Kode Produk", "Nama Produk", "Kategori", "Harga", "Stok", "Foto"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) return ImageIcon.class;
                return Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(60);
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Sembunyikan kolom ID
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);

        // Center align untuk beberapa kolom
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Kode
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Harga
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Stok

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2, true));

        // ================= FILTER REALTIME =================
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        Runnable filterAction = () -> {
            String text = tfSearch.getText().trim();
            if (text.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2, 3));
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
        JButton btnKelolaKategori = createButton("Kategori", "/img/category.png", new Color(155, 89, 182));
        JButton btnRefresh = createButton("Refresh", "/img/refresh.png", new Color(241, 196, 15));
        JButton btnBack   = createButton("Kembali", "/img/back.png", new Color(153, 153, 153));

        btnPanel.add(btnTambah);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnKelolaKategori);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnBack);

        // ================= LAYOUT UTAMA =================
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(sp, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        
        add(mainPanel);

        // ================= ACTIONS =================
        // Test koneksi database
        testDatabaseConnection();
        
        // Cek dan tambah data sample jika kosong
        addSampleDataIfEmpty();
        
        // Load data
        loadData();

        btnTambah.addActionListener(e -> {
            table.clearSelection();
            selectedId = null;
            tambahProduk();
        });
        
        btnEdit.addActionListener(e -> editProduk());
        btnHapus.addActionListener(e -> hapusProduk());
        
        btnRefresh.addActionListener(e -> {
            tfSearch.setText("");
            sorter.setRowFilter(null);
            loadData();
        });
        
        btnKelolaKategori.addActionListener(e -> {
            KelolaKategoriDialog kategoriDialog = new KelolaKategoriDialog(this);
            kategoriDialog.setVisible(true);
            loadData();
        });
        
        btnBack.addActionListener(e -> {
            DashboardAdmin dash = new DashboardAdmin(admin);
            dash.setVisible(true);
            dispose();
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    int modelRow = table.convertRowIndexToModel(row);
                    selectedId = (Integer) model.getValueAt(modelRow, 0);
                    selectedFoto = null;
                }
            }
        });

        // Double click untuk edit
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editProduk();
                }
            }
        });
    }

    // ================= TEST KONEKSI DATABASE =================
    private void testDatabaseConnection() {
        try (Connection conn = Database.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connected successfully");
            } else {
                System.out.println("❌ Database connection failed");
                JOptionPane.showMessageDialog(this, 
                    "❌ Gagal terhubung ke database!",
                    "Error Koneksi",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.out.println("❌ Database error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "❌ Error koneksi database: " + e.getMessage(),
                "Error Koneksi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= TAMBAH DATA SAMPLE JIKA KOSONG =================
    private void addSampleDataIfEmpty() {
        try (Connection conn = Database.getConnection()) {
            if (conn == null) return;
            
            // Cek apakah ada data produk
            String checkSql = "SELECT COUNT(*) as count FROM produk";
            Statement checkStmt = conn.createStatement();
            ResultSet rs = checkStmt.executeQuery(checkSql);
            
            if (rs.next() && rs.getInt("count") == 0) {
                System.out.println("📝 Menambah data sample...");
                
                int option = JOptionPane.showConfirmDialog(this,
                    "Database produk kosong. Tambah data sample untuk testing?",
                    "Data Sample",
                    JOptionPane.YES_NO_OPTION);
                
                if (option == JOptionPane.YES_OPTION) {
                    // Tambah kategori sample
                    String insertKategori = "INSERT IGNORE INTO kategori (nama_kategori) VALUES " +
                                           "('Elektronik'), ('Makanan'), ('Minuman'), ('Peralatan')";
                    Statement stmtKategori = conn.createStatement();
                    stmtKategori.execute(insertKategori);
                    
                    // Tambah produk sample
                    String insertProduk = "INSERT INTO produk (kode, nama_produk, kategori_id, harga, stok) VALUES " +
                                         "('ELK-001', 'Laptop ASUS ROG', 1, 15000000, 5), " +
                                         "('ELK-002', 'Smartphone Samsung', 1, 5000000, 15), " +
                                         "('MKN-001', 'Roti Tawar', 2, 12000, 30), " +
                                         "('MKN-002', 'Biscuit Coklat', 2, 8000, 50), " +
                                         "('MNM-001', 'Air Mineral 600ml', 3, 4000, 100), " +
                                         "('MNM-002', 'Jus Jeruk', 3, 8000, 40), " +
                                         "('PRL-001', 'Pensil 2B', 4, 3000, 80)";
                    Statement stmtProduk = conn.createStatement();
                    stmtProduk.execute(insertProduk);
                    
                    System.out.println("✅ Data sample berhasil ditambahkan");
                    JOptionPane.showMessageDialog(this, 
                        "✅ Data sample berhasil ditambahkan!",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                System.out.println("✅ Database sudah berisi " + rs.getInt("count") + " produk");
            }
        } catch (Exception e) {
            System.out.println("❌ Gagal menambah data sample: " + e.getMessage());
        }
    }

    // ================= LOAD DATA PRODUK =================
    private void loadData() {
        model.setRowCount(0);
        selectedId = null;
        
        System.out.println("🔄 Memuat data produk...");
        
        try (Connection conn = Database.getConnection()) {
            // Pastikan koneksi berhasil
            if (conn == null) {
                JOptionPane.showMessageDialog(this, 
                    "❌ Gagal terhubung ke database!",
                    "Error Koneksi",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String sql = "SELECT p.id, p.kode, p.nama_produk, k.nama_kategori, p.harga, p.stok, p.foto " +
                        "FROM produk p " +
                        "LEFT JOIN kategori k ON p.kategori_id = k.id " +
                        "ORDER BY p.id DESC";
            
            System.out.println("📝 Executing query: " + sql);
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                int id = rs.getInt("id");
                String kode = rs.getString("kode");
                String namaProduk = rs.getString("nama_produk");
                String kategori = rs.getString("nama_kategori");
                long harga = rs.getLong("harga");
                int stok = rs.getInt("stok");
                String fotoPath = rs.getString("foto");
                
                // Debug setiap row
                System.out.println("📦 Row " + rowCount + ": " + id + " | " + kode + " | " + namaProduk);
                
                // Handle null values
                if (kode == null || kode.trim().isEmpty()) {
                    kode = "-";
                }
                if (kategori == null) {
                    kategori = "Tanpa Kategori";
                }
                if (namaProduk == null) {
                    namaProduk = "-";
                }
                
                // Handle foto
                ImageIcon imgIcon = createPlaceholderIcon();
                if (fotoPath != null && !fotoPath.trim().isEmpty()) {
                    try {
                        String fixedPath = resolveImagePath(fotoPath);
                        File file = new File(fixedPath);
                        if (file.exists()) {
                            Image img = new ImageIcon(fixedPath).getImage()
                                    .getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                            imgIcon = new ImageIcon(img);
                        }
                    } catch (Exception ex) {
                        System.out.println("❌ Gagal load foto: " + fotoPath + " - " + ex.getMessage());
                    }
                }

                model.addRow(new Object[]{
                    id,
                    kode,
                    namaProduk,
                    kategori,
                    "Rp " + df.format(harga),
                    stok,
                    imgIcon
                });
            }
            
            System.out.println("✅ Data loaded: " + rowCount + " produk");
            
            if (rowCount == 0) {
                System.out.println("⚠️ Tidak ada data produk di database");
                JOptionPane.showMessageDialog(this, 
                    "📝 Tidak ada data produk.\nSilakan tambah produk baru.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "❌ Gagal memuat data produk!\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
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
            lblHeader.setIconTextGap(15);
        } else {
            lblHeader = new JLabel(text, JLabel.CENTER);
        }
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));
        return lblHeader;
    }

    // ================= GENERATE KODE BERDASARKAN KATEGORI =================
    private String generateKodeProduk(Connection conn, String kategoriNama) throws SQLException {
        String inisialKategori;
        if (kategoriNama == null || kategoriNama.trim().isEmpty() || kategoriNama.equals("Tanpa Kategori")) {
            inisialKategori = "UMUM";
        } else {
            inisialKategori = kategoriNama.toUpperCase().replaceAll("[^A-Z]", "");
            if (inisialKategori.length() > 3) {
                inisialKategori = inisialKategori.substring(0, 3);
            } else if (inisialKategori.length() < 3 && inisialKategori.length() > 0) {
                while (inisialKategori.length() < 3) {
                    inisialKategori += "X";
                }
            } else if (inisialKategori.isEmpty()) {
                inisialKategori = "UMUM";
            }
        }

        String sql = "SELECT kode FROM produk p " +
                    "LEFT JOIN kategori k ON p.kategori_id = k.id " +
                    "WHERE (k.nama_kategori = ? OR (? = 'Tanpa Kategori' AND p.kategori_id IS NULL)) " +
                    "AND kode LIKE ? " +
                    "ORDER BY LENGTH(kode) DESC, kode DESC LIMIT 1";
        
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, kategoriNama);
        pst.setString(2, kategoriNama);
        pst.setString(3, inisialKategori + "-%");
        ResultSet rs = pst.executeQuery();

        int nextNum = 1;
        if (rs.next()) {
            String lastKode = rs.getString("kode");
            if (lastKode != null && lastKode.startsWith(inisialKategori + "-")) {
                try {
                    String numberPart = lastKode.substring(inisialKategori.length() + 1);
                    nextNum = Integer.parseInt(numberPart) + 1;
                } catch (NumberFormatException e) {
                    // Continue dengan angka berikutnya
                }
            }
        }
        
        return String.format("%s-%03d", inisialKategori, nextNum);
    }

    private void tambahProduk() {
        JTextField tfNama = new JTextField();
        JTextField tfHarga = new JTextField();
        JTextField tfStok = new JTextField("0");
        formatRupiah(tfHarga);

        JComboBox<String> cbKategori = new JComboBox<>();
        loadKategoriFromDatabase(cbKategori);

        JButton btnPilihFoto = new JButton("📁 Pilih Foto");
        JLabel lblFoto = new JLabel("Belum ada foto dipilih");
        lblFoto.setForeground(Color.GRAY);
        final String[] pathFoto = {null};

        btnPilihFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Gambar (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                pathFoto[0] = file.getAbsolutePath();
                lblFoto.setText(file.getName());
                lblFoto.setForeground(Color.BLACK);
            }
        });

        Object[] message = {
            "Nama Produk:*", tfNama,
            "Kategori:", cbKategori,
            "Harga:*", tfHarga,
            "Stok:*", tfStok,
            "Foto:", btnPilihFoto, lblFoto
        };

        int option = JOptionPane.showConfirmDialog(this, message, "➕ Tambah Produk Baru", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if (tfNama.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Nama produk harus diisi!");
                return;
            }
            if (tfHarga.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Harga harus diisi!");
                return;
            }

            try (Connection conn = Database.getConnection()) {
                String kategoriNama = cbKategori.getSelectedItem().toString();
                String kodeBaru = generateKodeProduk(conn, kategoriNama);
                int kategoriId = getKategoriIdByName(conn, kategoriNama);

                long harga = parseRupiah(tfHarga.getText());
                int stok = Integer.parseInt(tfStok.getText());

                String sql = "INSERT INTO produk (kode, nama_produk, kategori_id, harga, stok, foto) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, kodeBaru);
                pst.setString(2, tfNama.getText().trim());
                pst.setInt(3, kategoriId);
                pst.setLong(4, harga);
                pst.setInt(5, stok);
                pst.setString(6, pathFoto[0]);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, 
                    "✅ Produk berhasil ditambahkan!\n" +
                    "Kode: " + kodeBaru + "\n" +
                    "Nama: " + tfNama.getText().trim());
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "❌ Gagal menambah produk!\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editProduk() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Pilih produk yang akan diedit!");
            return;
        }

        int row = table.getSelectedRow(); 
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "⚠️ Pilih produk yang akan diedit!");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        String namaAwal = model.getValueAt(modelRow, 2).toString();
        String kategoriAwal = model.getValueAt(modelRow, 3).toString();
        String kodeAwal = model.getValueAt(modelRow, 1).toString();
        String hargaAwal = model.getValueAt(modelRow, 4).toString().replace("Rp ", "").replaceAll("[^0-9]", "");
        String stokAwal = model.getValueAt(modelRow, 5).toString();

        JTextField tfNama = new JTextField(namaAwal);
        JTextField tfHarga = new JTextField(hargaAwal);
        JTextField tfStok = new JTextField(stokAwal);
        formatRupiah(tfHarga);

        JComboBox<String> cbKategori = new JComboBox<>();
        loadKategoriFromDatabase(cbKategori);
        cbKategori.setSelectedItem(kategoriAwal);

        JButton btnPilihFoto = new JButton("📁 Pilih Foto Baru");
        JLabel lblFoto = new JLabel("");
        lblFoto.setForeground(Color.GRAY);
        final String[] pathFoto = {null};

        btnPilihFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Gambar (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                pathFoto[0] = file.getAbsolutePath();
                lblFoto.setText(file.getName());
                lblFoto.setForeground(Color.BLACK);
            }
        });

        Object[] message = {
            "Nama Produk:*", tfNama,
            "Kategori:", cbKategori,
            "Harga:*", tfHarga,
            "Stok:*", tfStok,
            "Foto:", btnPilihFoto, lblFoto
        };

        int option = JOptionPane.showConfirmDialog(this, message, "✏️ Edit Produk", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if (tfNama.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Nama produk harus diisi!");
                return;
            }

            try (Connection conn = Database.getConnection()) {
                long harga = parseRupiah(tfHarga.getText());
                int stok = Integer.parseInt(tfStok.getText());
                
                String kategoriNama = cbKategori.getSelectedItem().toString();
                int kategoriId = getKategoriIdByName(conn, kategoriNama);

                // Auto generate kode jika kategori berubah
                String kodeBaru = kodeAwal;
                if (!kategoriAwal.equals(kategoriNama)) {
                    kodeBaru = generateKodeProduk(conn, kategoriNama);
                }

                String sql = "UPDATE produk SET nama_produk=?, kategori_id=?, harga=?, stok=?, foto=COALESCE(?, foto), kode=? WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, tfNama.getText().trim());
                pst.setInt(2, kategoriId);
                pst.setLong(3, harga);
                pst.setInt(4, stok);
                pst.setString(5, pathFoto[0]);
                pst.setString(6, kodeBaru);
                pst.setInt(7, selectedId);
                pst.executeUpdate();

                String messageText = "✅ Produk berhasil diupdate!";
                if (!kategoriAwal.equals(kategoriNama)) {
                    messageText += "\nKode baru: " + kodeBaru + " (kategori berubah)";
                }
                JOptionPane.showMessageDialog(this, messageText);
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "❌ Gagal update produk!\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void hapusProduk() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Pilih produk yang akan dihapus!");
            return;
        }

        int row = table.getSelectedRow();
        String kodeProduk = model.getValueAt(table.convertRowIndexToModel(row), 1).toString();
        String namaProduk = model.getValueAt(table.convertRowIndexToModel(row), 2).toString();
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "<html><b>Hapus Produk?</b><br><br>" +
            "Kode: <b>" + kodeProduk + "</b><br>" +
            "Nama: <b>" + namaProduk + "</b><br><br>" +
            "Data yang dihapus tidak dapat dikembalikan!",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Database.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM detail_transaksi WHERE produk_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, selectedId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                    "❌ Produk tidak dapat dihapus!\n" +
                    "Produk ini sudah pernah digunakan dalam transaksi.\n" +
                    "Untuk menjaga integritas data, hapus tidak diperbolehkan.",
                    "Produk Terpakai",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String sql = "DELETE FROM produk WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, selectedId);
            int affectedRows = pst.executeUpdate();
            
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "✅ Produk berhasil dihapus!");
                loadData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "❌ Gagal menghapus produk!\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
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
            String insertSql = "INSERT INTO kategori (nama_kategori) VALUES (?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, kategoriName);
            insertStmt.executeUpdate();
            
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        return 1;
    }

    private JButton createButton(String text, String iconPath, Color bgColor) {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        } catch (Exception e) {
            System.out.println("Gagal load icon: " + iconPath);
        }

        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 38));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(10);
        btn.setHorizontalAlignment(SwingConstants.CENTER);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { 
                btn.setBackground(bgColor.darker()); 
            }
            public void mouseExited(java.awt.event.MouseEvent evt) { 
                btn.setBackground(bgColor); 
            }
        });
        return btn;
    }

    private ImageIcon createPlaceholderIcon() {
        BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Background
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, 50, 50);
        
        // Border
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRect(0, 0, 49, 49);
        
        // Icon camera
        g2d.setColor(new Color(150, 150, 150));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(10, 8, 30, 24); // Lens
        g2d.drawRect(18, 32, 14, 8); // Body
        
        g2d.dispose();
        return new ImageIcon(image);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManajemenProduk("Admin").setVisible(true));
    }

    private String resolveImagePath(String dbPath) {
        if (dbPath == null) return null;
        File f = new File(dbPath);
        if (f.exists() && f.isAbsolute()) {
            return dbPath;
        }
        // Try looking in src/img
        String relativePath = "src/img/" + new File(dbPath).getName();
        if (new File(relativePath).exists()) {
            return relativePath;
        }
        // Try looking in current dir (if src is not needed)
        String localPath = "img/" + new File(dbPath).getName();
        if (new File(localPath).exists()) {
            return localPath;
        }
        // Fallback to absolute path constructed from project dir
        return System.getProperty("user.dir") + "/src/img/" + new File(dbPath).getName();
    }
}
