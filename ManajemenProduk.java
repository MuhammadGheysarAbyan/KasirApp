import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;

public class ManajemenProduk extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private Integer selectedId = null;
    private String selectedFoto = null;
    private String admin;
    private String shift = "Shift 1"; // default shift

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
                    double value = Double.parseDouble(text);
                    java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("id", "ID"));
                    field.setText(format.format(value).replace(",00", ""));
                } catch (Exception ex) { }
            }
        });
    }

    private double parseRupiah(String text) {
        if (text == null || text.isEmpty()) return 0;
        text = text.replaceAll("[^0-9]", "");
        if (text.isEmpty()) return 0;
        return Double.parseDouble(text);
    }

    public ManajemenProduk() {
        this("Guest");
    }

    public ManajemenProduk(String admin) {
        this.admin = admin;

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
        model = new DefaultTableModel(new String[]{"Kode Produk", "Nama Produk", "Kategori", "Harga", "Stok", "Foto"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return ImageIcon.class;
                return Object.class;
            }
        };
        table = new JTable(model);
        table.setRowHeight(60);
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);

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
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2));
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

        btnPanel.add(btnTambah);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnBack);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // ================= ACTIONS =================
        loadData();

        btnTambah.addActionListener(e -> tambahProduk());
        btnEdit.addActionListener(e -> editProduk());
        btnHapus.addActionListener(e -> hapusProduk());
        btnBack.addActionListener(e -> {
            DashboardAdmin dash = new DashboardAdmin(admin);
            dash.setVisible(true);
            dispose();
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                selectedId = Integer.parseInt(getIdByRow(row));
                selectedFoto = null;
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

    private ImageIcon loadRoundedLogo(String path, int size) {
        try {
            if (path.startsWith("/")) path = path.substring(1);
            java.net.URL imgURL = getClass().getClassLoader().getResource(path);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);

                BufferedImage rounded = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = rounded.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Double(0, 0, size, size, size / 5, size / 5));
                g2.drawImage(scaledImage, 0, 0, null);
                g2.dispose();

                return new ImageIcon(rounded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ================= BUTTON =================
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


    // ================= LOAD DATA =================
    private void loadData() {
        model.setRowCount(0);
        selectedId = null;
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM produk ORDER BY id ASC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String kodeProduk = generateKodeProduk(id);
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
                        kodeProduk,
                        rs.getString("nama_produk"),
                        rs.getString("kategori"),
                        new DecimalFormat("#,###").format(rs.getDouble("harga")),
                        rs.getInt("stok"),
                        imgIcon
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load data!\n" + e.getMessage());
        }
    }

    // ================= GENERATE KODE PRODUK =================
    private String generateKodeProduk(int id) {
        return String.format("PRO-%04d", id);
    }

    // ================= CARI ID KOSONG UNTUK PRODUK BARU =================
    private int getNextAvailableId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM produk ORDER BY id ASC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        int expected = 1;
        while (rs.next()) {
            int id = rs.getInt("id");
            if (id != expected) return expected;
            expected++;
        }
        return expected;
    }
    
    // ================= GET ID BY ROW =================
private String getIdByRow(int row) {
    try (Connection conn = Database.getConnection()) {
        String kode = model.getValueAt(row, 0).toString();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT id FROM produk ORDER BY id DESC");
        while (rs.next()) {
            int id = rs.getInt("id");
            if (generateKodeProduk(id).equals(kode)) {
                return String.valueOf(id);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

private void tambahProduk() {
    JTextField tfNama = new JTextField();
    JTextField tfKategori = new JTextField(); // kategori manual
    JTextField tfHarga = new JTextField();
    JTextField tfStok = new JTextField();
    formatRupiah(tfHarga);

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
        "Kategori:", tfKategori,
        "Harga:", tfHarga,
        "Stok:", tfStok,
        btnPilihFoto, lblFoto
    };

    int option = JOptionPane.showConfirmDialog(this, message, "Tambah Produk", JOptionPane.OK_CANCEL_OPTION);
    if (option == JOptionPane.OK_OPTION) {
        try (Connection conn = Database.getConnection()) {
            int newId = getNextAvailableId(conn);

            double harga = parseRupiah(tfHarga.getText());
            int stok = Integer.parseInt(tfStok.getText());

            String sql = "INSERT INTO produk (id, nama_produk, kategori, harga, stok, foto) VALUES (?,?,?,?,?,?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, newId);
            pst.setString(2, tfNama.getText().trim());
            pst.setString(3, tfKategori.getText().trim()); // manual input
            pst.setDouble(4, harga);
            pst.setInt(5, stok);
            pst.setString(6, pathFoto[0]);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan!\nKode Produk: " + generateKodeProduk(newId));
            loadData();
        } catch (Exception e) {
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
    String namaAwal = model.getValueAt(row, 1).toString();
    String kategoriAwal = model.getValueAt(row, 2).toString();
    String hargaAwal = model.getValueAt(row, 3).toString().replaceAll("[^0-9]", "");
    String stokAwal = model.getValueAt(row, 4).toString();

    JTextField tfNama = new JTextField(namaAwal);
    JTextField tfKategori = new JTextField(kategoriAwal); // kategori manual
    JTextField tfHarga = new JTextField("Rp" + new DecimalFormat("#,###").format(Double.parseDouble(hargaAwal)));
    JTextField tfStok = new JTextField(stokAwal);
    formatRupiah(tfHarga);

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
        "Kategori:", tfKategori,
        "Harga:", tfHarga,
        "Stok:", tfStok,
        btnPilihFoto, lblFoto
    };

    int option = JOptionPane.showConfirmDialog(this, message, "Edit Produk", JOptionPane.OK_CANCEL_OPTION);
    if (option == JOptionPane.OK_OPTION) {
        try (Connection conn = Database.getConnection()) {
            double harga = parseRupiah(tfHarga.getText());
            int stok = Integer.parseInt(tfStok.getText());

            String sql = "UPDATE produk SET nama_produk=?, kategori=?, harga=?, stok=?, foto=IFNULL(?, foto) WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, tfNama.getText().trim());
            pst.setString(2, tfKategori.getText().trim()); // manual input
            pst.setDouble(3, harga);
            pst.setInt(4, stok);
            pst.setString(5, pathFoto[0]);
            pst.setInt(6, selectedId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Produk berhasil diupdate!\nKode Produk: " + generateKodeProduk(selectedId));
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal update produk!\n" + e.getMessage());
        }
    }
}


    // ================= HAPUS PRODUK =================
    private void hapusProduk() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Pilih produk dulu!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus produk ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Database.getConnection()) {
            String sql = "DELETE FROM produk WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, selectedId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Produk berhasil dihapus!");
            loadData();
        } catch (SQLIntegrityConstraintViolationException fkEx) {
            JOptionPane.showMessageDialog(this,
                    "❌ Produk ini sudah pernah dipakai di transaksi.\nTidak bisa dihapus untuk menjaga riwayat penjualan.",
                    "Produk Terpakai",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus produk!\n" + e.getMessage());
        }
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManajemenProduk("Tester").setVisible(true));
    }
}
