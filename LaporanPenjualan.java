import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class LaporanPenjualan extends JFrame {

    private String admin;
    JTable table;
    DefaultTableModel model;
    JSpinner dariTanggal, sampaiTanggal;
    JComboBox<String> comboFilter, comboKasir, comboKategori, comboStatus;
    DecimalFormat df;
    private JLabel lblTotalOmset, lblTotalTransaksi, lblRataTransaksi;
    private JPanel statsPanel;

    public LaporanPenjualan(String usernameAdmin) {
        this.admin = usernameAdmin;

        // Format angka ribuan pakai titik
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        df = new DecimalFormat("#,###", symbols);

        setTitle("📊 Laporan Penjualan - " + admin);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ================= PANEL UTAMA DENGAN GRADIENT =================
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20)) {
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
        add(mainPanel);

        // ================= HEADER =================
        JLabel lblHeader;
        try {
            ImageIcon iconHeader = new ImageIcon(getClass().getResource("/img/report.png"));
            Image img = iconHeader.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            iconHeader = new ImageIcon(img);
            lblHeader = new JLabel("Laporan Penjualan", iconHeader, JLabel.CENTER);
            lblHeader.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblHeader.setIconTextGap(10);
        } catch (Exception e) {
            lblHeader = new JLabel("Laporan Penjualan", JLabel.CENTER);
        }
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // ================= STATISTIK PANEL =================
        statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        lblTotalOmset = createStatCard("Total Omset", "Rp 0", new Color(0, 102, 204));
        lblTotalTransaksi = createStatCard("Total Transaksi", "0", new Color(40, 167, 69));
        lblRataTransaksi = createStatCard("Rata-rata/Transaksi", "Rp 0", new Color(255, 193, 7));
        
        statsPanel.add(lblTotalOmset);
        statsPanel.add(lblTotalTransaksi);
        statsPanel.add(lblRataTransaksi);
        
        mainPanel.add(statsPanel, BorderLayout.CENTER);

        // ================= CONTENT BOX =================
        JPanel contentBox = new JPanel(new BorderLayout(10, 10));
        contentBox.setBackground(new Color(255, 255, 255, 245));
        contentBox.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 2));
        mainPanel.add(contentBox, BorderLayout.CENTER);

        // ================= FILTER PANEL =================
        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));
        filterPanel.setOpaque(false);

        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterBox.setOpaque(false);

        // ComboBox untuk jenis filter
        comboFilter = new JComboBox<>(new String[]{"Semua", "Harian", "Bulanan", "Rentang Tanggal"});
        comboFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // ComboBox untuk kasir
        comboKasir = new JComboBox<>(new String[]{"Semua Kasir"});
        comboKasir.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // ComboBox untuk kategori
        comboKategori = new JComboBox<>(new String[]{"Semua Kategori"});
        comboKategori.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // ComboBox untuk status transaksi
        comboStatus = new JComboBox<>(new String[]{"Semua Status", "selesai", "pending", "dibatalkan"});
        comboStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        dariTanggal = new JSpinner(new SpinnerDateModel());
        dariTanggal.setEditor(new JSpinner.DateEditor(dariTanggal, "yyyy-MM-dd"));
        sampaiTanggal = new JSpinner(new SpinnerDateModel());
        sampaiTanggal.setEditor(new JSpinner.DateEditor(sampaiTanggal, "yyyy-MM-dd"));

        JButton btnFilter = createButton("Filter", "/img/search.png", new Color(0, 153, 255));
        JButton btnReset = createButton("Reset", "/img/refresh.png", new Color(255, 153, 0));
        JButton btnExport = createButton("Export", "/img/export.png", new Color(40, 167, 69));

        filterBox.add(new JLabel("Jenis Filter:"));
        filterBox.add(comboFilter);
        filterBox.add(new JLabel("Kasir:"));
        filterBox.add(comboKasir);
        filterBox.add(new JLabel("Kategori:"));
        filterBox.add(comboKategori);
        filterBox.add(new JLabel("Status:"));
        filterBox.add(comboStatus);
        filterBox.add(new JLabel("Dari:"));
        filterBox.add(dariTanggal);
        filterBox.add(new JLabel("Sampai:"));
        filterBox.add(sampaiTanggal);
        filterBox.add(btnFilter);
        filterBox.add(btnReset);
        filterBox.add(btnExport);

        filterPanel.add(filterBox, BorderLayout.CENTER);
        contentBox.add(filterPanel, BorderLayout.NORTH);

        // ================= TABEL =================
        model = new DefaultTableModel(
                new String[]{
                    "Foto Produk", "Kode Produk", "Nama Produk", "Kategori", 
                    "Harga Satuan", "Qty Terjual", "Total Harga", "Kasir", 
                    "Status", "Tanggal Transaksi"
                }, 0
        ) {
            public boolean isCellEditable(int row, int column) { return false; }
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? ImageIcon.class : Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(70);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Center alignment untuk beberapa kolom
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Right alignment untuk kolom numeric
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int i = 1; i < model.getColumnCount(); i++) {
            if (i == 4 || i == 5 || i == 6) { // Kolom harga dan qty
                table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        JScrollPane sp = new JScrollPane(table);
        contentBox.add(sp, BorderLayout.CENTER);

        // ================= BUTTON PANEL =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setOpaque(false);
        
        // TOMBOL DETAIL TRANSAKSI - WARNA UNGU (dipindah ke kiri)
        JButton btnDetail = createButton("Detail Transaksi", "/img/detail.png", new Color(128, 0, 128)); // Warna ungu
        
        // TOMBOL KEMBALI (dipindah ke kanan)
        JButton btnBack = createButton("Kembali", "/img/back.png", new Color(153, 153, 153));
        
        // Urutan tombol ditukar: Detail dulu, kemudian Kembali
        btnPanel.add(btnDetail);
        btnPanel.add(btnBack);
        
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        // ================= EVENT HANDLERS =================
        btnBack.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardAdmin(admin).setVisible(true));
        });

        btnDetail.addActionListener(e -> showDetailTransaksi());
        
        btnExport.addActionListener(e -> exportToExcel());

        // ================= LOAD DATA AWAL =================
        loadKasirData();
        loadKategoriData();
        loadData(null, null, null, null, null, null);
        updateStatistics(null, null, null, null, null);

        // ================= AKSI FILTER =================
        btnFilter.addActionListener(e -> applyFilter());
        
        btnReset.addActionListener(e -> {
            comboFilter.setSelectedIndex(0);
            comboKasir.setSelectedIndex(0);
            comboKategori.setSelectedIndex(0);
            comboStatus.setSelectedIndex(0);
            dariTanggal.setValue(new java.util.Date());
            sampaiTanggal.setValue(new java.util.Date());
            loadData(null, null, null, null, null, null);
            updateStatistics(null, null, null, null, null);
        });

        comboFilter.addActionListener(e -> {
            String selectedFilter = (String) comboFilter.getSelectedItem();
            switch (selectedFilter) {
                case "Harian":
                    setTanggalHariIni();
                    break;
                case "Bulanan":
                    setTanggalBulanIni();
                    break;
                case "Rentang Tanggal":
                    // Biarkan user memilih tanggal manual
                    break;
                default:
                    break;
            }
        });
    }

    private JLabel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(color);
        
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(Color.DARK_GRAY);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        JLabel wrapper = new JLabel() {
            @Override
            public String getText() {
                return value;
            }
            
            @Override
            public void setText(String text) {
                valueLabel.setText(text);
            }
        };
        wrapper.setLayout(new BorderLayout());
        wrapper.add(card);
        
        return wrapper;
    }

    private void setTanggalHariIni() {
        Calendar cal = Calendar.getInstance();
        dariTanggal.setValue(cal.getTime());
        sampaiTanggal.setValue(cal.getTime());
    }

    private void setTanggalBulanIni() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        dariTanggal.setValue(cal.getTime());
        
        cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        sampaiTanggal.setValue(cal.getTime());
    }

    private void applyFilter() {
        try {
            String jenisFilter = (String) comboFilter.getSelectedItem();
            String selectedKasir = (String) comboKasir.getSelectedItem();
            String selectedKategori = (String) comboKategori.getSelectedItem();
            String selectedStatus = (String) comboStatus.getSelectedItem();
            
            String kasirUsername = selectedKasir.equals("Semua Kasir") ? null : selectedKasir;
            String kategori = selectedKategori.equals("Semua Kategori") ? null : selectedKategori;
            String status = selectedStatus.equals("Semua Status") ? null : selectedStatus;

            java.util.Date dariDate = (java.util.Date) dariTanggal.getValue();
            java.util.Date sampaiDate = (java.util.Date) sampaiTanggal.getValue();
            
            Calendar c = Calendar.getInstance();
            c.setTime(sampaiDate);
            c.add(Calendar.DAY_OF_MONTH, 1);
            
            java.sql.Date sqlDari = new java.sql.Date(dariDate.getTime());
            java.sql.Date sqlSampai = new java.sql.Date(c.getTimeInMillis());

            if ("Semua".equals(jenisFilter)) {
                sqlDari = null;
                sqlSampai = null;
            }

            loadData(sqlDari, sqlSampai, jenisFilter, kasirUsername, kategori, status);
            updateStatistics(sqlDari, sqlSampai, kasirUsername, kategori, status);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Format tanggal tidak valid!\n" + ex.getMessage());
        }
    }

    private void loadKasirData() {
        try (Connection conn = Database.getConnection()) {
            if (conn == null) throw new SQLException("Koneksi database gagal!");

            String sql = "SELECT username FROM users WHERE role = 'kasir' ORDER BY username";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                comboKasir.addItem(rs.getString("username"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data kasir:\n" + e.getMessage());
        }
    }

    private void loadKategoriData() {
        try (Connection conn = Database.getConnection()) {
            if (conn == null) throw new SQLException("Koneksi database gagal!");

            String sql = "SELECT nama_kategori FROM kategori ORDER BY nama_kategori";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                comboKategori.addItem(rs.getString("nama_kategori"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data kategori:\n" + e.getMessage());
        }
    }

    private JButton createButton(String text, String iconPath, Color bgColor) {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource(iconPath));
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
        } catch (Exception e) {
            System.out.println("Gagal load icon: " + iconPath);
        }
        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(8);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return btn;
    }

    private void loadData(java.sql.Date dari, java.sql.Date sampai, String jenisFilter, 
                         String kasirUsername, String kategori, String status) {
        model.setRowCount(0);

        try (Connection conn = Database.getConnection()) {
            if (conn == null) throw new SQLException("Koneksi database gagal!");

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ")
               .append("p.id, p.kode, p.nama_produk, p.foto, p.harga, ")
               .append("k.nama_kategori, ")
               .append("SUM(d.qty) AS totalQty, ")
               .append("SUM(d.qty * d.harga) AS totalHarga, ")
               .append("u.username AS kasir, u.nama AS nama_kasir, ")
               .append("t.status, t.tanggal, t.waktu ")
               .append("FROM transaksi t ")
               .append("JOIN detail_transaksi d ON t.id = d.transaksi_id ")
               .append("JOIN produk p ON d.produk_id = p.id ")
               .append("JOIN kategori k ON p.kategori_id = k.id ")
               .append("JOIN users u ON t.kasir_id = u.id ")
               .append("WHERE 1=1 ");

            // Filter berdasarkan tanggal
            if (dari != null && sampai != null && !"Semua".equals(jenisFilter)) {
                sql.append("AND t.tanggal BETWEEN ? AND ? ");
            }

            // Filter berdasarkan kasir
            if (kasirUsername != null && !kasirUsername.isEmpty()) {
                sql.append("AND u.username = ? ");
            }

            // Filter berdasarkan kategori
            if (kategori != null && !kategori.isEmpty()) {
                sql.append("AND k.nama_kategori = ? ");
            }

            // Filter berdasarkan status
            if (status != null && !status.isEmpty()) {
                sql.append("AND t.status = ? ");
            }

            sql.append("GROUP BY p.id, p.kode, p.nama_produk, p.foto, p.harga, ")
               .append("k.nama_kategori, u.username, u.nama, t.status, t.tanggal, t.waktu ")
               .append("ORDER BY t.tanggal DESC, t.waktu DESC");

            PreparedStatement pst = conn.prepareStatement(sql.toString());
            int paramIndex = 1;

            if (dari != null && sampai != null && !"Semua".equals(jenisFilter)) {
                pst.setDate(paramIndex++, dari);
                pst.setDate(paramIndex++, sampai);
            }

            if (kasirUsername != null && !kasirUsername.isEmpty()) {
                pst.setString(paramIndex++, kasirUsername);
            }

            if (kategori != null && !kategori.isEmpty()) {
                pst.setString(paramIndex++, kategori);
            }

            if (status != null && !status.isEmpty()) {
                pst.setString(paramIndex, status);
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                // Load foto produk
                ImageIcon foto = null;
                try {
                    String path = rs.getString("foto");
                    if (path != null && !path.isEmpty()) {
                        Image img = new ImageIcon(path).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                        foto = new ImageIcon(img);
                    }
                } catch (Exception ex) {
                    System.out.println("Gagal load foto produk: " + ex.getMessage());
                }

                // Format tanggal dan waktu
                String tanggalWaktu = rs.getDate("tanggal") + " " + rs.getTime("waktu");

                model.addRow(new Object[]{
                    foto,
                    rs.getString("kode"),
                    rs.getString("nama_produk"),
                    rs.getString("nama_kategori"),
                    "Rp " + df.format(rs.getDouble("harga")),
                    rs.getInt("totalQty"),
                    "Rp " + df.format(rs.getDouble("totalHarga")),
                    rs.getString("nama_kasir") + " (" + rs.getString("kasir") + ")",
                    rs.getString("status"),
                    tanggalWaktu
                });
            }

            // Update judul dengan info filter
            String filterInfo = getFilterInfo(jenisFilter, dari, sampai, kasirUsername, kategori, status);
            setTitle("📊 Laporan Penjualan Lengkap - " + admin + " | " + filterInfo);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat laporan penjualan:\n" + e.getMessage());
        }
    }

    private void updateStatistics(java.sql.Date dari, java.sql.Date sampai, 
                                 String kasirUsername, String kategori, String status) {
        try (Connection conn = Database.getConnection()) {
            if (conn == null) throw new SQLException("Koneksi database gagal!");

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ")
               .append("COUNT(DISTINCT t.id) AS total_transaksi, ")
               .append("SUM(d.qty * d.harga) AS total_omset, ")
               .append("AVG(d.qty * d.harga) AS rata_rata ")
               .append("FROM transaksi t ")
               .append("JOIN detail_transaksi d ON t.id = d.transaksi_id ")
               .append("JOIN produk p ON d.produk_id = p.id ")
               .append("JOIN kategori k ON p.kategori_id = k.id ")
               .append("JOIN users u ON t.kasir_id = u.id ")
               .append("WHERE 1=1 ");

            if (dari != null && sampai != null) {
                sql.append("AND t.tanggal BETWEEN ? AND ? ");
            }

            if (kasirUsername != null && !kasirUsername.isEmpty()) {
                sql.append("AND u.username = ? ");
            }

            if (kategori != null && !kategori.isEmpty()) {
                sql.append("AND k.nama_kategori = ? ");
            }

            if (status != null && !status.isEmpty()) {
                sql.append("AND t.status = ? ");
            }

            PreparedStatement pst = conn.prepareStatement(sql.toString());
            int paramIndex = 1;

            if (dari != null && sampai != null) {
                pst.setDate(paramIndex++, dari);
                pst.setDate(paramIndex++, sampai);
            }

            if (kasirUsername != null && !kasirUsername.isEmpty()) {
                pst.setString(paramIndex++, kasirUsername);
            }

            if (kategori != null && !kategori.isEmpty()) {
                pst.setString(paramIndex++, kategori);
            }

            if (status != null && !status.isEmpty()) {
                pst.setString(paramIndex, status);
            }

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int totalTransaksi = rs.getInt("total_transaksi");
                double totalOmset = rs.getDouble("total_omset");
                double rataRata = rs.getDouble("rata_rata");

                lblTotalTransaksi.setText(String.valueOf(totalTransaksi));
                lblTotalOmset.setText("Rp " + df.format(totalOmset));
                lblRataTransaksi.setText("Rp " + df.format(rataRata));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFilterInfo(String jenisFilter, java.sql.Date dari, java.sql.Date sampai, 
                               String kasirUsername, String kategori, String status) {
        StringBuilder info = new StringBuilder();
        
        if (jenisFilter != null && !"Semua".equals(jenisFilter)) {
            info.append(jenisFilter);
        }
        
        if (dari != null && sampai != null && !"Semua".equals(jenisFilter)) {
            info.append(" (").append(dari).append(" s/d ").append(sampai).append(")");
        }
        
        if (kasirUsername != null && !kasirUsername.isEmpty()) {
            info.append(" | Kasir: ").append(kasirUsername);
        }
        
        if (kategori != null && !kategori.isEmpty()) {
            info.append(" | Kategori: ").append(kategori);
        }
        
        if (status != null && !status.isEmpty()) {
            info.append(" | Status: ").append(status);
        }
        
        return info.toString().isEmpty() ? "Semua Data" : info.toString();
    }

    private void showDetailTransaksi() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi terlebih dahulu!");
            return;
        }

        String kodeProduk = (String) table.getValueAt(selectedRow, 1);
        String kasirInfo = (String) table.getValueAt(selectedRow, 7);
        String tanggal = (String) table.getValueAt(selectedRow, 9);

        // Extract username kasir dari format "Nama (username)"
        String usernameKasir = kasirInfo.substring(kasirInfo.lastIndexOf("(") + 1, kasirInfo.lastIndexOf(")"));

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT t.id, t.kode_transaksi, t.total, t.status, " +
                        "u.nama AS nama_kasir, u.shift, " +
                        "p.nama_produk, k.nama_kategori, " +
                        "d.qty, d.harga, d.subtotal " +
                        "FROM transaksi t " +
                        "JOIN users u ON t.kasir_id = u.id " +
                        "JOIN detail_transaksi d ON t.id = d.transaksi_id " +
                        "JOIN produk p ON d.produk_id = p.id " +
                        "JOIN kategori k ON p.kategori_id = k.id " +
                        "WHERE p.kode = ? AND u.username = ? AND t.tanggal = ? " +
                        "ORDER BY t.id DESC LIMIT 1";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kodeProduk);
            pst.setString(2, usernameKasir);
            pst.setDate(3, java.sql.Date.valueOf(tanggal.split(" ")[0]));

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String detail = String.format(
                    "Detail Transaksi:\n\n" +
                    "ID Transaksi: %d\n" +
                    "Kode Transaksi: %s\n" +
                    "Total: Rp %s\n" +
                    "Status: %s\n" +
                    "Kasir: %s\n" +
                    "Shift: %s\n" +
                    "Produk: %s\n" +
                    "Kategori: %s\n" +
                    "Qty: %d\n" +
                    "Harga Satuan: Rp %s\n" +
                    "Subtotal: Rp %s",
                    rs.getInt("id"),
                    rs.getString("kode_transaksi"),
                    df.format(rs.getDouble("total")),
                    rs.getString("status"),
                    rs.getString("nama_kasir"),
                    rs.getString("shift"),
                    rs.getString("nama_produk"),
                    rs.getString("nama_kategori"),
                    rs.getInt("qty"),
                    df.format(rs.getDouble("harga")),
                    df.format(rs.getDouble("subtotal"))
                );

                JOptionPane.showMessageDialog(this, detail, "Detail Transaksi", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat detail transaksi!");
        }
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan Excel");
        fileChooser.setSelectedFile(new java.io.File("laporan_penjualan.xlsx"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            // Implementasi export Excel bisa ditambahkan di sini
            JOptionPane.showMessageDialog(this, 
                "Fitur export Excel akan diimplementasi!\nFile: " + file.getAbsolutePath(),
                "Export Excel", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LaporanPenjualan("admin").setVisible(true));
    }
}