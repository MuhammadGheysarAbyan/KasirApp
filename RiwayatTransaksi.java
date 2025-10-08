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

        filterPanel.add(new JLabel("Dari:"));
        filterPanel.add(dariTanggal);
        filterPanel.add(new JLabel("Sampai:"));
        filterPanel.add(sampaiTanggal);
        filterPanel.add(btnFilter);

        contentBox.add(filterPanel, BorderLayout.NORTH);

        // ================= TABEL =================
        model = new DefaultTableModel(
                new String[]{"ID Transaksi","Tanggal","Kasir","Nama Produk","Jumlah","Harga","Foto"},0
        ) {
            public boolean isCellEditable(int row,int column){ return false; }
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 6 ? ImageIcon.class : Object.class;
            }
        };
        table = new JTable(model);
        table.setRowHeight(60);

        // Center alignment for some columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane sp = new JScrollPane(table);
        contentBox.add(sp, BorderLayout.CENTER);

        // ================= BUTTON BACK =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
        btnPanel.setBackground(new Color(255,255,255,0));
        JButton btnBack = createButton("Kembali", "/img/back.png", new Color(153,153,153));
        btnPanel.add(btnBack);
        background.add(btnPanel, BorderLayout.SOUTH);

        btnBack.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardKasir(kasir).setVisible(true));
        });

        // ================= LOAD DATA =================
        loadData(null,null);

        btnFilter.addActionListener(e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dari = sdf.format(dariTanggal.getValue());
            String sampai = sdf.format(sampaiTanggal.getValue());
            loadData(dari,sampai);
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

            // ✅ QUERY FIX DENGAN FOTO PRODUK
            String sql = "SELECT t.id AS idTransaksi, t.tanggal, u.username, p.nama_produk, d.qty, d.harga, p.foto " +
                         "FROM transaksi t " +
                         "JOIN users u ON t.kasir_id = u.id " +
                         "JOIN detail_transaksi d ON t.id = d.transaksi_id " +
                         "JOIN produk p ON d.produk_id = p.id ";

            if (dari != null && sampai != null) {
                sql += "WHERE DATE(t.tanggal) BETWEEN ? AND ? ";
            }

            sql += "ORDER BY t.id DESC";

            PreparedStatement pst = conn.prepareStatement(sql);
            if (dari != null && sampai != null) {
                pst.setString(1, dari);
                pst.setString(2, sampai);
            }

            ResultSet rs = pst.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

            while (rs.next()) {
                int idTransaksi = rs.getInt("idTransaksi");
                String kode = String.format("TRX-%03d", idTransaksi);
                Date tgl = rs.getTimestamp("tanggal");
                String username = rs.getString("username");
                String produk = rs.getString("nama_produk");
                int qty = rs.getInt("qty");
                double harga = rs.getDouble("harga");

                // === Load foto produk ===
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
                        kode,
                        sdf.format(tgl),
                        username,
                        produk,
                        qty,
                        "Rp " + df.format(harga),
                        foto
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Gagal load data: "+e.getMessage());
        }
    }

    public static void main(String[] args){
        String kasirLogin = "messi";
        SwingUtilities.invokeLater(() -> new RiwayatTransaksi(kasirLogin).setVisible(true));
    }
}
