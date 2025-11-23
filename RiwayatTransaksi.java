import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RiwayatTransaksi extends JFrame {

    private String kasir; // username kasir yang login
    JTable table;
    DefaultTableModel model;
    JSpinner dariTanggal, sampaiTanggal;
    DecimalFormat df = new DecimalFormat("#,###");

    public RiwayatTransaksi(String kasir) {
        this.kasir = kasir;

        setTitle("📜 Riwayat Transaksi - " + kasir);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ================= BACKGROUND GRADIENT =================
        JPanel background = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0,102,204), 0, getHeight(), Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        background.setLayout(new BorderLayout(0,20));
        add(background);

        // ================= HEADER =================
        JLabel lblHeader;
        try {
            ImageIcon iconHeader = new ImageIcon(getClass().getResource("/img/riwayat.png"));
            Image img = iconHeader.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            iconHeader = new ImageIcon(img);
            lblHeader = new JLabel("Riwayat Transaksi", iconHeader, JLabel.CENTER);
            lblHeader.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblHeader.setIconTextGap(10);
        } catch (Exception e) {
            lblHeader = new JLabel("Riwayat Transaksi", JLabel.CENTER);
        }
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));
        background.add(lblHeader, BorderLayout.NORTH);

        // ================= KOTAK ISI =================
        JPanel contentBox = new JPanel(new BorderLayout(10,10));
        contentBox.setBackground(new Color(255,255,255,245));
        contentBox.setBorder(BorderFactory.createLineBorder(new Color(0,102,204),2));
        background.add(contentBox, BorderLayout.CENTER);

        // ================= FILTER TANGGAL =================
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(new Color(255,255,255,0));

        dariTanggal = new JSpinner(new SpinnerDateModel());
        dariTanggal.setEditor(new JSpinner.DateEditor(dariTanggal, "yyyy-MM-dd"));
        sampaiTanggal = new JSpinner(new SpinnerDateModel());
        sampaiTanggal.setEditor(new JSpinner.DateEditor(sampaiTanggal, "yyyy-MM-dd"));

        JButton btnFilter = createButton("Filter", "/img/search.png", new Color(0,153,255));
        JButton btnReset = createButton("Reset", "/img/refresh.png", new Color(255, 153, 0));

        filterPanel.add(new JLabel("Dari:"));
        filterPanel.add(dariTanggal);
        filterPanel.add(new JLabel("Sampai:"));
        filterPanel.add(sampaiTanggal);
        filterPanel.add(btnFilter);
        filterPanel.add(btnReset);

        contentBox.add(filterPanel, BorderLayout.NORTH);

        // ================= TABEL =================
        model = new DefaultTableModel(
                new String[]{"Kode Transaksi","Tanggal","Waktu","Kasir","Nama Produk","Kategori","Jumlah","Harga","Subtotal","Foto"},0
        ) {
            public boolean isCellEditable(int row,int column){ return false; }
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 9 ? ImageIcon.class : Object.class;
            }
        };
        table = new JTable(model);
        table.setRowHeight(60);

        // Center alignment for some columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Right alignment untuk kolom numeric
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        JScrollPane sp = new JScrollPane(table);
        contentBox.add(sp, BorderLayout.CENTER);

        // ================= BUTTON BACK =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
        btnPanel.setBackground(new Color(255,255,255,0));
        JButton btnBack = createButton("Kembali", "/img/back.png", new Color(153,153,153));
        JButton btnDetail = createButton("Detail", "/img/detail.png", new Color(128, 0, 128));
        btnPanel.add(btnDetail);
        btnPanel.add(btnBack);
        background.add(btnPanel, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardKasir(kasir).setVisible(true));
        });

        btnDetail.addActionListener(e -> showDetailTransaksi());

        // ================= LOAD DATA =================
        loadData(null,null);

        btnFilter.addActionListener(e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dari = sdf.format(dariTanggal.getValue());
            String sampai = sdf.format(sampaiTanggal.getValue());
            loadData(dari,sampai);
        });

        btnReset.addActionListener(e -> {
            dariTanggal.setValue(new Date());
            sampaiTanggal.setValue(new Date());
            loadData(null, null);
        });
    }

    private JButton createButton(String text, String iconPath, Color bgColor){
        ImageIcon icon = null;
        try{
            icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(14,14,Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        } catch(Exception e){
            System.out.println("Gagal load icon: "+iconPath);
        }
        JButton btn = new JButton(text,icon);
        btn.setFont(new Font("Segoe UI",Font.PLAIN,14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(8);
        btn.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        return btn;
    }

    private void loadData(String dari, String sampai) {
        model.setRowCount(0);
        try (Connection conn = Database.getConnection()) {

            String sql = "SELECT " +
                         "t.kode_transaksi, t.tanggal, t.waktu, " +
                         "u.nama AS nama_kasir, u.username, " +
                         "p.nama_produk, p.foto, " +
                         "k.nama_kategori, " +
                         "d.qty, d.harga, d.subtotal " +
                         "FROM transaksi t " +
                         "JOIN users u ON t.kasir_id = u.id " +
                         "JOIN detail_transaksi d ON t.id = d.transaksi_id " +
                         "JOIN produk p ON d.produk_id = p.id " +
                         "JOIN kategori k ON p.kategori_id = k.id " +
                         "WHERE 1=1 ";

            if (dari != null && sampai != null) {
                sql += "AND DATE(t.tanggal) BETWEEN ? AND ? ";
            }

            // Jika kasir (bukan admin), hanya tampilkan transaksi kasir tersebut
            if (!kasir.equals("admin")) {
                sql += "AND u.username = ? ";
            }

            sql += "ORDER BY t.tanggal DESC, t.waktu DESC, t.id DESC";

            PreparedStatement pst = conn.prepareStatement(sql);
            int paramIndex = 1;
            
            if (dari != null && sampai != null) {
                pst.setString(paramIndex++, dari);
                pst.setString(paramIndex++, sampai);
            }
            
            if (!kasir.equals("admin")) {
                pst.setString(paramIndex, kasir);
            }

            ResultSet rs = pst.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            while (rs.next()) {
                String kodeTransaksi = rs.getString("kode_transaksi");
                Date tanggal = rs.getDate("tanggal");
                Time waktu = rs.getTime("waktu");
                String namaKasir = rs.getString("nama_kasir");
                String usernameKasir = rs.getString("username");
                String produk = rs.getString("nama_produk");
                String kategori = rs.getString("nama_kategori");
                int qty = rs.getInt("qty");
                double harga = rs.getDouble("harga");
                double subtotal = rs.getDouble("subtotal");

                // Format info kasir
                String infoKasir = namaKasir + " (" + usernameKasir + ")";

                // Load foto produk
                ImageIcon foto = null;
                try {
                    String path = rs.getString("foto");
                    if (path != null && !path.isEmpty()) {
                        Image img = new ImageIcon(path).getImage()
                                .getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                        foto = new ImageIcon(img);
                    }
                } catch (Exception ex) {
                    System.out.println("Gagal load foto produk: " + ex.getMessage());
                }

                model.addRow(new Object[]{
                        kodeTransaksi,
                        dateFormat.format(tanggal),
                        timeFormat.format(waktu),
                        infoKasir,
                        produk,
                        kategori,
                        qty,
                        "Rp " + df.format(harga),
                        "Rp " + df.format(subtotal),
                        foto
                });
            }

            // Update judul dengan info filter
            String filterInfo = "Semua Data";
            if (dari != null && sampai != null) {
                filterInfo = "Periode: " + dari + " s/d " + sampai;
            }
            setTitle("📜 Riwayat Transaksi - " + kasir + " | " + filterInfo);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Gagal load data: "+e.getMessage());
        }
    }

    private void showDetailTransaksi() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi terlebih dahulu!");
            return;
        }

        String kodeTransaksi = (String) table.getValueAt(selectedRow, 0);
        String tanggal = (String) table.getValueAt(selectedRow, 1);
        String waktu = (String) table.getValueAt(selectedRow, 2);

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT " +
                        "t.kode_transaksi, t.total, t.status, " +
                        "u.nama AS nama_kasir, u.username, u.shift, " +
                        "p.nama_produk, k.nama_kategori, " +
                        "d.qty, d.harga, d.subtotal " +
                        "FROM transaksi t " +
                        "JOIN users u ON t.kasir_id = u.id " +
                        "JOIN detail_transaksi d ON t.id = d.transaksi_id " +
                        "JOIN produk p ON d.produk_id = p.id " +
                        "JOIN kategori k ON p.kategori_id = k.id " +
                        "WHERE t.kode_transaksi = ? " +
                        "ORDER BY p.nama_produk";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kodeTransaksi);
            ResultSet rs = pst.executeQuery();

            StringBuilder detail = new StringBuilder();
            double totalTransaksi = 0;
            boolean firstRow = true;

            while (rs.next()) {
                if (firstRow) {
                    detail.append("=== DETAIL TRANSAKSI ===\n\n");
                    detail.append("Kode Transaksi: ").append(rs.getString("kode_transaksi")).append("\n");
                    detail.append("Tanggal       : ").append(tanggal).append(" ").append(waktu).append("\n");
                    detail.append("Kasir         : ").append(rs.getString("nama_kasir")).append("\n");
                    detail.append("Username      : ").append(rs.getString("username")).append("\n");
                    detail.append("Shift         : ").append(rs.getString("shift")).append("\n");
                    detail.append("Status        : ").append(rs.getString("status")).append("\n");
                    detail.append("\n=== ITEM PEMBELIAN ===\n\n");
                    firstRow = false;
                }

                String produk = rs.getString("nama_produk");
                String kategori = rs.getString("nama_kategori");
                int qty = rs.getInt("qty");
                double harga = rs.getDouble("harga");
                double subtotal = rs.getDouble("subtotal");
                totalTransaksi += subtotal;

                detail.append(String.format("Produk   : %s\n", produk));
                detail.append(String.format("Kategori : %s\n", kategori));
                detail.append(String.format("Qty      : %d\n", qty));
                detail.append(String.format("Harga    : Rp %s\n", df.format(harga)));
                detail.append(String.format("Subtotal : Rp %s\n", df.format(subtotal)));
                detail.append("────────────────────────\n");
            }

            if (!firstRow) {
                detail.append(String.format("\nTOTAL TRANSAKSI: Rp %s", df.format(totalTransaksi)));
                
                JTextArea textArea = new JTextArea(detail.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 400));
                
                JOptionPane.showMessageDialog(this, scrollPane, 
                    "Detail Transaksi - " + kodeTransaksi, 
                    JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat detail transaksi!");
        }
    }

    public static void main(String[] args){
        String kasirLogin = "kasir1";
        SwingUtilities.invokeLater(() -> new RiwayatTransaksi(kasirLogin).setVisible(true));
    }
}