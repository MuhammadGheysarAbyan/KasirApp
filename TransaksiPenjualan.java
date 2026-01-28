import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

public class TransaksiPenjualan extends JFrame {
    JTable tableProduk, tableKeranjang;
    DefaultTableModel modelProduk, modelKeranjang;
    JTextField txtCari, txtTotal, txtBayar;
    String kasirUsername;
    DecimalFormat df;

    public TransaksiPenjualan(String kasirUsername) {
        this.kasirUsername = kasirUsername;

        // Format angka Indonesia (Rp)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        df = new DecimalFormat("#,###", symbols);

        setTitle("🛒 Transaksi Penjualan - " + kasirUsername);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ================= BACKGROUND GRADIENT =================
        JPanel mainPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 102, 204), 0, getHeight(), Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));
        add(mainPanel);

        // ================= HEADER =================
        JLabel lblHeader;
        try {
            ImageIcon iconHeader = new ImageIcon(getClass().getResource("/img/chart.png"));
            Image img = iconHeader.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            iconHeader = new ImageIcon(img);
            lblHeader = new JLabel("Transaksi Penjualan", iconHeader, JLabel.CENTER);
            lblHeader.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblHeader.setIconTextGap(15);
        } catch (Exception e) {
            lblHeader = new JLabel("Transaksi Penjualan", JLabel.CENTER);
        }
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // ================= PANEL TENGAH =================
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setOpaque(false);

        // ===== PANEL PRODUK =====
        JPanel panelProduk = new JPanel(new BorderLayout(5, 5));
        panelProduk.setOpaque(false);

        txtCari = new JTextField();
        JButton btnCari = new JButton("🔍 Cari");
        btnCari.setBackground(new Color(0, 153, 255));
        btnCari.setForeground(Color.WHITE);
        btnCari.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.add(txtCari, BorderLayout.CENTER);
        searchPanel.add(btnCari, BorderLayout.EAST);
        panelProduk.add(searchPanel, BorderLayout.NORTH);

        modelProduk = new DefaultTableModel(new String[] { "Kode Produk", "Nama", "Kategori", "Harga", "Stok", "Foto" },
                0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return col == 5 ? ImageIcon.class : Object.class;
            }
        };
        tableProduk = new JTable(modelProduk);
        tableProduk.setRowHeight(60);
        JScrollPane spProduk = new JScrollPane(tableProduk);
        panelProduk.add(spProduk, BorderLayout.CENTER);

        JButton btnTambahKeranjang = new JButton("➕ Tambah ke Keranjang");
        btnTambahKeranjang.setBackground(new Color(0, 204, 102));
        btnTambahKeranjang.setForeground(Color.WHITE);
        btnTambahKeranjang.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel bottomPanelProduk = new JPanel(new BorderLayout());
        bottomPanelProduk.setOpaque(false);
        bottomPanelProduk.setBorder(BorderFactory.createEmptyBorder(0, 0, 36, 0));
        bottomPanelProduk.add(btnTambahKeranjang, BorderLayout.CENTER);
        panelProduk.add(bottomPanelProduk, BorderLayout.SOUTH);

        // ===== PANEL KERANJANG =====
        JPanel panelKeranjang = new JPanel(new BorderLayout(5, 5));
        panelKeranjang.setOpaque(false);

        modelKeranjang = new DefaultTableModel(
                new String[] { "Kode Produk", "Nama", "Harga", "Jumlah", "Subtotal", "Foto" }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return col == 5 ? ImageIcon.class : Object.class;
            }
        };
        tableKeranjang = new JTable(modelKeranjang);
        tableKeranjang.setRowHeight(60);
        JScrollPane spKeranjang = new JScrollPane(tableKeranjang);
        panelKeranjang.add(spKeranjang, BorderLayout.CENTER);

        JPanel bottomKeranjang = new JPanel(new GridLayout(4, 2, 10, 10));
        bottomKeranjang.setOpaque(false);
        txtTotal = new JTextField("Rp 0");
        txtTotal.setEditable(false);
        txtBayar = new JTextField();

        JButton btnBayar = new JButton("💵 Bayar");
        btnBayar.setBackground(new Color(0, 102, 204));
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnHapus = new JButton("❌ Batal");
        btnHapus.setBackground(new Color(255, 77, 77));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnBack = new JButton("⬅️ Kembali");
        btnBack.setBackground(new Color(153, 153, 153));
        btnBack.setForeground(Color.WHITE);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));

        bottomKeranjang.add(new JLabel("Total:"));
        bottomKeranjang.add(txtTotal);
        bottomKeranjang.add(new JLabel("Bayar:"));
        bottomKeranjang.add(txtBayar);
        bottomKeranjang.add(btnBayar);
        bottomKeranjang.add(btnHapus);
        panelKeranjang.add(bottomKeranjang, BorderLayout.SOUTH);

        JPanel topBackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBackPanel.setOpaque(false);
        topBackPanel.add(btnBack);
        panelKeranjang.add(topBackPanel, BorderLayout.NORTH);

        centerPanel.add(panelProduk);
        centerPanel.add(panelKeranjang);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ================= LOAD PRODUK =================
        loadProduk("");

        // ================= AKSI TOMBOL =================
        btnCari.addActionListener(e -> loadProduk(txtCari.getText()));
        btnTambahKeranjang.addActionListener(e -> tambahKeranjang());
        btnHapus.addActionListener(e -> {
            modelKeranjang.setRowCount(0);
            txtTotal.setText("Rp 0");
            txtBayar.setText("");
        });
        btnBayar.addActionListener(e -> bayar());
        btnBack.addActionListener(e -> {
            new DashboardKasir(kasirUsername).setVisible(true);
            dispose();
        });

        // === Auto format input bayar jadi Rupiah ===
        txtBayar.getDocument().addDocumentListener(new DocumentListener() {
            boolean editing = false;

            public void insertUpdate(DocumentEvent e) {
                formatBayar();
            }

            public void removeUpdate(DocumentEvent e) {
                formatBayar();
            }

            public void changedUpdate(DocumentEvent e) {
                formatBayar();
            }

            private void formatBayar() {
                if (editing)
                    return;
                editing = true;
                SwingUtilities.invokeLater(() -> {
                    try {
                        String text = txtBayar.getText().replaceAll("[^0-9]", "");
                        if (text.isEmpty()) {
                            txtBayar.setText("");
                        } else {
                            long value = Long.parseLong(text);
                            txtBayar.setText("Rp " + df.format(value));
                        }
                    } catch (Exception ex) {
                        txtBayar.setText("");
                    } finally {
                        editing = false;
                    }
                });
            }
        });

        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                loadProduk(txtCari.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                loadProduk(txtCari.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                loadProduk(txtCari.getText());
            }
        });
    }

    private void loadProduk(String keyword) {
        modelProduk.setRowCount(0);
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT p.*, k.nama_kategori FROM produk p " +
                    "JOIN kategori k ON p.kategori_id = k.id " +
                    "WHERE p.nama_produk LIKE ? OR p.kode LIKE ? " +
                    "ORDER BY p.nama_produk";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String kodeProduk = rs.getString("kode");
                double harga = rs.getDouble("harga");
                ImageIcon icon = null;
                try {
                    String path = rs.getString("foto");
                    if (path != null && !path.isEmpty()) {
                        String fixedPath = resolveImagePath(path);
                        java.io.File file = new java.io.File(fixedPath);
                        if (file.exists()) {
                            Image img = new ImageIcon(fixedPath).getImage()
                                    .getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                            icon = new ImageIcon(img);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Gagal load gambar: " + ex.getMessage());
                }

                modelProduk.addRow(new Object[] {
                        kodeProduk,
                        rs.getString("nama_produk"),
                        rs.getString("nama_kategori"),
                        "Rp " + df.format(harga),
                        rs.getInt("stok"),
                        icon
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal load produk!");
        }
    }

    private void tambahKeranjang() {
        int row = tableProduk.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk dulu!");
            return;
        }

        String kode = (String) modelProduk.getValueAt(row, 0);
        String nama = (String) modelProduk.getValueAt(row, 1);
        String hargaStr = modelProduk.getValueAt(row, 3).toString().replace("Rp", "").replace(".", "").trim();
        double harga = Double.parseDouble(hargaStr);
        int stok = (int) modelProduk.getValueAt(row, 4);
        ImageIcon foto = (ImageIcon) modelProduk.getValueAt(row, 5);

        String jumlahStr = JOptionPane.showInputDialog(this, "Masukkan jumlah untuk " + nama + ":");
        if (jumlahStr == null)
            return;

        try {
            int jumlah = Integer.parseInt(jumlahStr);
            if (jumlah <= 0) {
                JOptionPane.showMessageDialog(this, "Jumlah harus lebih dari 0!");
                return;
            }
            if (jumlah > stok) {
                JOptionPane.showMessageDialog(this, "Stok tidak cukup! Stok tersedia: " + stok);
                return;
            }

            // Cek apakah produk sudah ada di keranjang
            for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                String existingKode = (String) modelKeranjang.getValueAt(i, 0);
                if (existingKode.equals(kode)) {
                    int existingJumlah = (int) modelKeranjang.getValueAt(i, 3);
                    int newJumlah = existingJumlah + jumlah;
                    if (newJumlah > stok) {
                        JOptionPane.showMessageDialog(this, "Total jumlah melebihi stok! Stok tersedia: " + stok);
                        return;
                    }
                    // Update jumlah dan subtotal
                    modelKeranjang.setValueAt(newJumlah, i, 3);
                    double newSubtotal = newJumlah * harga;
                    modelKeranjang.setValueAt("Rp " + df.format(newSubtotal), i, 4);
                    hitungTotal();
                    return;
                }
            }

            // Jika produk belum ada di keranjang, tambahkan baru
            double subtotal = harga * jumlah;
            modelKeranjang.addRow(new Object[] {
                    kode, nama, "Rp " + df.format(harga), jumlah, "Rp " + df.format(subtotal), foto
            });
            hitungTotal();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Masukkan jumlah yang valid!");
        }
    }

    private void hitungTotal() {
        double total = 0;
        for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
            String subStr = modelKeranjang.getValueAt(i, 4).toString().replace("Rp", "").replace(".", "").trim();
            total += Double.parseDouble(subStr);
        }
        txtTotal.setText("Rp " + df.format(total));
    }

    private void bayar() {
        if (modelKeranjang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double total = Double.parseDouble(txtTotal.getText().replace("Rp", "").replace(".", "").trim());
            if (txtBayar.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan jumlah pembayaran!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double bayar = Double.parseDouble(txtBayar.getText().replace("Rp", "").replace(".", "").trim());
            if (bayar < total) {
                JOptionPane.showMessageDialog(this, "Uang tidak cukup!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double kembalian = bayar - total;

            // Pindahkan koneksi ke luar try-with-resources agar bisa diakses di catch
            Connection conn = null;
            try {
                conn = Database.getConnection();
                conn.setAutoCommit(false);

                // Dapatkan ID kasir
                int idKasir = 0;
                String sqlKasir = "SELECT id FROM users WHERE username = ?";
                PreparedStatement pstKasir = conn.prepareStatement(sqlKasir);
                pstKasir.setString(1, kasirUsername);
                ResultSet rsKasir = pstKasir.executeQuery();
                if (rsKasir.next()) {
                    idKasir = rsKasir.getInt("id");
                }

                // Generate kode transaksi
                String kodeTransaksi = generateKodeTransaksi(conn);

                // Insert ke tabel transaksi
                String sqlTrans = "INSERT INTO transaksi (kode_transaksi, total, tanggal, kasir_id, status, waktu) " +
                        "VALUES (?, ?, CURDATE(), ?, 'selesai', CURTIME())";
                PreparedStatement pstTrans = conn.prepareStatement(sqlTrans, Statement.RETURN_GENERATED_KEYS);
                pstTrans.setString(1, kodeTransaksi);
                pstTrans.setDouble(2, total);
                pstTrans.setInt(3, idKasir);
                pstTrans.executeUpdate();

                ResultSet gk = pstTrans.getGeneratedKeys();
                int idTransaksi = 0;
                if (gk.next()) {
                    idTransaksi = gk.getInt(1);
                }

                // Insert detail transaksi dan update stok
                for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                    String kodeProduk = (String) modelKeranjang.getValueAt(i, 0);
                    int qty = (int) modelKeranjang.getValueAt(i, 3);
                    String hargaStr = modelKeranjang.getValueAt(i, 2).toString().replace("Rp", "").replace(".", "")
                            .trim();
                    double harga = Double.parseDouble(hargaStr);

                    // Dapatkan ID produk dari kode
                    int idProduk = 0;
                    String sqlProduk = "SELECT id FROM produk WHERE kode = ?";
                    PreparedStatement pstProduk = conn.prepareStatement(sqlProduk);
                    pstProduk.setString(1, kodeProduk);
                    ResultSet rsProduk = pstProduk.executeQuery();
                    if (rsProduk.next()) {
                        idProduk = rsProduk.getInt("id");
                    }

                    // Insert detail transaksi TANPA kolom subtotal (karena generated column)
                    String sqlDet = "INSERT INTO detail_transaksi (transaksi_id, produk_id, qty, harga) " +
                            "VALUES (?, ?, ?, ?)";
                    PreparedStatement pstDet = conn.prepareStatement(sqlDet);
                    pstDet.setInt(1, idTransaksi);
                    pstDet.setInt(2, idProduk);
                    pstDet.setInt(3, qty);
                    pstDet.setDouble(4, harga);
                    pstDet.executeUpdate();

                    // Update stok produk
                    String sqlUpdateStok = "UPDATE produk SET stok = stok - ? WHERE id = ?";
                    PreparedStatement pstStok = conn.prepareStatement(sqlUpdateStok);
                    pstStok.setInt(1, qty);
                    pstStok.setInt(2, idProduk);
                    pstStok.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Transaksi berhasil!\nKode Transaksi: " + kodeTransaksi +
                                "\nTotal: Rp " + df.format(total) +
                                "\nBayar: Rp " + df.format(bayar) +
                                "\nKembalian: Rp " + df.format(kembalian),
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);

                cetakStruk(idTransaksi, kodeTransaksi, total, bayar, kembalian);

                // Reset form
                modelKeranjang.setRowCount(0);
                txtTotal.setText("Rp 0");
                txtBayar.setText("");
                loadProduk(""); // Refresh stok produk

            } catch (Exception ex) {
                // Rollback jika terjadi error
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal melakukan transaksi: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Tutup koneksi di finally block
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Masukkan nominal yang valid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generateKodeTransaksi(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM transaksi WHERE tanggal = CURDATE()";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        int count = 0;
        if (rs.next()) {
            count = rs.getInt("count") + 1;
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyMMdd");
        String datePart = sdf.format(new java.util.Date());
        return "TRX-" + datePart + "-" + String.format("%03d", count);
    }

    public void cetakStruk(int idTransaksi, String kodeTransaksi, double total, double bayar, double kembalian) {
        try {
            JFrame frame = new JFrame("🧾 Struk Pembayaran");
            frame.setSize(380, 560);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panelUtama = new JPanel(new BorderLayout());
            panelUtama.setBackground(Color.WHITE);

            // HEADER
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
            headerPanel.setBackground(new Color(0, 102, 204));
            headerPanel.setPreferredSize(new Dimension(380, 60));

            JLabel lblLogo = new JLabel();
            try {
                ImageIcon logoIcon = new ImageIcon(getClass().getResource("/img/logo.jpg"));
                Image img = logoIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                lblLogo.setText("");
            }

            JLabel lblNamaToko = new JLabel("TOKO KOMPUTER BYNEST");
            lblNamaToko.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
            lblNamaToko.setForeground(Color.WHITE);

            headerPanel.add(lblLogo);
            headerPanel.add(lblNamaToko);
            panelUtama.add(headerPanel, BorderLayout.NORTH);

            // AREA STRUK
            JTextArea area = new JTextArea();
            area.setEditable(false);
            area.setFont(new Font("Consolas", Font.PLAIN, 13));
            area.setForeground(Color.DARK_GRAY);
            area.setBackground(Color.WHITE);
            area.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
            area.setFocusable(false);

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String tanggal = sdf.format(new java.util.Date());

            StringBuilder sb = new StringBuilder();
            sb.append("              TOKO KOMPUTER BYNEST\n");
            sb.append("     Jl. Merdeka No.99, Bandung - 40123\n");
            sb.append("             Telp. 0812-3456-7890\n");
            sb.append("========================================\n");
            sb.append(String.format("Kode Transaksi : %s\n", kodeTransaksi));
            sb.append("Tanggal      : ").append(tanggal).append("\n");
            sb.append("Kasir        : ").append(kasirUsername).append("\n");
            sb.append("----------------------------------------\n");
            sb.append("Nama Barang         Qty   Harga     Total\n");
            sb.append("----------------------------------------\n");

            for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                String nama = modelKeranjang.getValueAt(i, 1).toString();
                int qty = Integer.parseInt(modelKeranjang.getValueAt(i, 3).toString());
                String hargaStr = modelKeranjang.getValueAt(i, 2).toString();
                String subStr = modelKeranjang.getValueAt(i, 4).toString();

                sb.append(String.format("%-15s %3d %10s %10s\n",
                        nama.length() > 15 ? nama.substring(0, 15) : nama,
                        qty, hargaStr, subStr));
            }

            sb.append("----------------------------------------\n");
            sb.append(String.format("TOTAL       : Rp %s\n", df.format(total)));
            sb.append(String.format("BAYAR       : Rp %s\n", df.format(bayar)));
            sb.append(String.format("KEMBALIAN   : Rp %s\n", df.format(kembalian)));
            sb.append("========================================\n");
            sb.append("  TERIMA KASIH TELAH BERBELANJA DI BYNEST\n");
            sb.append("========================================\n");

            area.setText(sb.toString());

            JScrollPane scrollPane = new JScrollPane(area);
            scrollPane.setBorder(null);
            panelUtama.add(scrollPane, BorderLayout.CENTER);

            // TOMBOL OK
            JButton btnOk = new JButton("OK");
            btnOk.setBackground(new Color(0, 102, 204));
            btnOk.setForeground(Color.WHITE);
            btnOk.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnOk.setFocusPainted(false);
            btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOk.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            JPanel footerPanel = new JPanel();
            footerPanel.setBackground(Color.WHITE);
            footerPanel.add(btnOk);
            panelUtama.add(footerPanel, BorderLayout.SOUTH);

            // Aksi tombol
            btnOk.addActionListener(e -> frame.dispose());

            frame.add(panelUtama);
            frame.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal mencetak struk: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new TransaksiPenjualan("admin").setVisible(true);
    }

    private String resolveImagePath(String dbPath) {
        if (dbPath == null)
            return null;
        java.io.File f = new java.io.File(dbPath);
        if (f.exists() && f.isAbsolute()) {
            return dbPath;
        }
        // Try looking in src/img
        String relativePath = "src/img/" + new java.io.File(dbPath).getName();
        if (new java.io.File(relativePath).exists()) {
            return relativePath;
        }
        // Try looking in current dir (if src is not needed)
        String localPath = "img/" + new java.io.File(dbPath).getName();
        if (new java.io.File(localPath).exists()) {
            return localPath;
        }
        // Fallback to absolute path constructed from project dir
        return System.getProperty("user.dir") + "/src/img/" + new java.io.File(dbPath).getName();
    }
}