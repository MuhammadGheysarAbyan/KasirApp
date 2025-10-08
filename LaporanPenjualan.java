import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

public class LaporanPenjualan extends JFrame {

    private String admin;
    JTable table;
    DefaultTableModel model;
    JSpinner dariTanggal, sampaiTanggal;
    DecimalFormat df;
    private JLabel lblTotalOmset;

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

        dariTanggal = new JSpinner(new SpinnerDateModel());
        dariTanggal.setEditor(new JSpinner.DateEditor(dariTanggal, "yyyy-MM-dd"));
        sampaiTanggal = new JSpinner(new SpinnerDateModel());
        sampaiTanggal.setEditor(new JSpinner.DateEditor(sampaiTanggal, "yyyy-MM-dd"));

        JButton btnFilter = createButton("Filter", "/img/search.png", new Color(0, 153, 255));
        filterBox.add(new JLabel("Dari:"));
        filterBox.add(dariTanggal);
        filterBox.add(new JLabel("Sampai:"));
        filterBox.add(sampaiTanggal);
        filterBox.add(btnFilter);

        // ================= OMSET PANEL =================
        JPanel omsetBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        omsetBox.setOpaque(false);
        JLabel lblOmsetTitle = new JLabel("Total Omset: ");
        lblOmsetTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalOmset = new JLabel("Rp 0");
        lblTotalOmset.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalOmset.setForeground(Color.WHITE);
        lblTotalOmset.setOpaque(true);
        lblTotalOmset.setBackground(new Color(0, 102, 204));
        lblTotalOmset.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        omsetBox.add(lblOmsetTitle);
        omsetBox.add(lblTotalOmset);

        filterPanel.add(filterBox, BorderLayout.WEST);
        filterPanel.add(omsetBox, BorderLayout.EAST);
        contentBox.add(filterPanel, BorderLayout.NORTH);

        // ================= TABEL =================
        model = new DefaultTableModel(
                new String[]{"Foto Produk", "Kode Produk", "Nama Produk", "Total Barang Terjual", "Total Harga Terjual", "Kasir"}, 0
        ) {
            public boolean isCellEditable(int row, int column) { return false; }
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? ImageIcon.class : Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(70);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // center beberapa kolom
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i <= 4; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane sp = new JScrollPane(table);
        contentBox.add(sp, BorderLayout.CENTER);

        // ================= BUTTON BACK =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setOpaque(false);
        JButton btnBack = createButton("Kembali", "/img/back.png", new Color(153, 153, 153));
        btnPanel.add(btnBack);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardAdmin(admin).setVisible(true));
        });

        // ================= LOAD DATA =================
        loadData(null, null);

        // ================= AKSI FILTER =================
        btnFilter.addActionListener(e -> {
            try {
                java.util.Date dariDate = (java.util.Date) dariTanggal.getValue();
                java.util.Date sampaiDate = (java.util.Date) sampaiTanggal.getValue();
                Calendar c = Calendar.getInstance();
                c.setTime(sampaiDate);
                c.add(Calendar.DAY_OF_MONTH, 1);
                loadData(new java.sql.Date(dariDate.getTime()), new java.sql.Date(c.getTimeInMillis()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Format tanggal tidak valid!\n" + ex.getMessage());
            }
        });
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

    private void loadData(java.sql.Date dari, java.sql.Date sampai) {
        model.setRowCount(0);
        double totalOmset = 0;

        try (Connection conn = Database.getConnection()) {
            if (conn == null) throw new SQLException("Koneksi database gagal!");

            String sql = "SELECT p.id, p.nama_produk, p.foto, SUM(d.qty) AS totalQty, " +
                    "SUM(d.qty*d.harga) AS totalHarga, u.username AS kasir " +
                    "FROM transaksi t " +
                    "JOIN detail_transaksi d ON t.id = d.transaksi_id " +
                    "JOIN produk p ON d.produk_id = p.id " +
                    "JOIN users u ON t.kasir_id = u.id ";

            if (dari != null && sampai != null) {
                sql += "WHERE t.tanggal BETWEEN ? AND ? ";
            }

            sql += "GROUP BY p.id, p.nama_produk, u.username ORDER BY p.id DESC";

            PreparedStatement pst = conn.prepareStatement(sql);
            if (dari != null && sampai != null) {
                pst.setDate(1, dari);
                pst.setDate(2, sampai);
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String kodeProduk = String.format("PRO-%04d", id);
                int qty = rs.getInt("totalQty");
                double total = rs.getDouble("totalHarga");
                String kasir = rs.getString("kasir");

                // === Load foto produk ===
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

                model.addRow(new Object[]{
                        foto,
                        kodeProduk,
                        rs.getString("nama_produk"),
                        qty,
                        "Rp " + df.format(total),
                        kasir
                });

                totalOmset += total;
            }

            lblTotalOmset.setText("Rp " + df.format(totalOmset));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat laporan penjualan:\n" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LaporanPenjualan("admin").setVisible(true));
    }
}
