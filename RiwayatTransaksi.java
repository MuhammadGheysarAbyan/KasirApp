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
        JOptionPane.showMessageDialog(this, "Pilih transaksi terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
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

        // Buat panel khusus dengan desain seperti struk
        JPanel panelStruk = new JPanel(new BorderLayout(0, 0));
        panelStruk.setBackground(Color.WHITE);
        panelStruk.setPreferredSize(new Dimension(500, 450));

        // ================= HEADER STRUK =================
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setPreferredSize(new Dimension(500, 70));
        
        // Load logo
        ImageIcon logoIcon = null;
        try {
            logoIcon = new ImageIcon(getClass().getResource("/img/logo.jpg"));
            if (logoIcon != null) {
                Image img = logoIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                logoIcon = new ImageIcon(img);
            }
        } catch (Exception e) {
            // Jika logo tidak ada, gunakan icon default
            System.out.println("Logo tidak ditemukan, menggunakan icon default");
        }
        
        JLabel lblLogo = new JLabel();
        if (logoIcon != null) {
            lblLogo.setIcon(logoIcon);
        }
        
        JLabel lblNamaToko = new JLabel("TOKO KOMPUTER BYNEST");
        lblNamaToko.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNamaToko.setForeground(Color.WHITE);
        
        headerPanel.add(lblLogo);
        headerPanel.add(lblNamaToko);
        panelStruk.add(headerPanel, BorderLayout.NORTH);

        // ================= BODY STRUK =================
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 10));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Informasi transaksi
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel lblJudul = new JLabel("📋 DETAIL TRANSAKSI", JLabel.CENTER);
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblJudul.setForeground(new Color(0, 102, 204));
        
        JLabel lblKode = new JLabel("Kode: " + kodeTransaksi);
        lblKode.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JLabel lblWaktu = new JLabel("Tanggal: " + tanggal + " " + waktu);
        lblWaktu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        infoPanel.add(lblJudul);
        infoPanel.add(lblKode);
        infoPanel.add(lblWaktu);
        
        bodyPanel.add(infoPanel, BorderLayout.NORTH);

        // ================= TABEL ITEM =================
        String[] columnNames = {"Produk", "Kategori", "Qty", "Harga", "Subtotal"};
        DefaultTableModel modelDetail = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tableDetail = new JTable(modelDetail);
        tableDetail.setRowHeight(25);
        tableDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableDetail.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableDetail.getTableHeader().setBackground(new Color(240, 240, 240));
        tableDetail.getTableHeader().setForeground(Color.DARK_GRAY);
        
        // Set alignment untuk kolom angka
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        tableDetail.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Qty
        tableDetail.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Harga
        tableDetail.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Subtotal
        
        JScrollPane scrollPane = new JScrollPane(tableDetail);
        scrollPane.setPreferredSize(new Dimension(450, 200));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        // ================= FOOTER =================
        JPanel footerPanel = new JPanel(new BorderLayout(0, 10));
        footerPanel.setBackground(Color.WHITE);
        
        double totalTransaksi = 0;
        String namaKasir = "";
        String shiftKasir = "";
        String status = "";
        boolean firstRow = true;

        while (rs.next()) {
            if (firstRow) {
                namaKasir = rs.getString("nama_kasir");
                shiftKasir = rs.getString("shift");
                status = rs.getString("status");
                totalTransaksi = rs.getDouble("total");
                
                // Tambahkan info kasir di info panel
                JLabel lblKasir = new JLabel("Kasir: " + namaKasir + " (" + rs.getString("username") + ")");
                lblKasir.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                infoPanel.add(lblKasir);
                
                JLabel lblShift = new JLabel("Shift: " + shiftKasir);
                lblShift.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                infoPanel.add(lblShift);
                
                JLabel lblStatus = new JLabel("Status: " + status);
                lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                infoPanel.add(lblStatus);
                
                // Garis pemisah
                JSeparator separator = new JSeparator();
                separator.setForeground(new Color(0, 102, 204));
                infoPanel.add(separator);
                
                firstRow = false;
            }

            String produk = rs.getString("nama_produk");
            String kategori = rs.getString("nama_kategori");
            int qty = rs.getInt("qty");
            double harga = rs.getDouble("harga");
            double subtotal = rs.getDouble("subtotal");

            modelDetail.addRow(new Object[]{
                produk,
                kategori,
                qty,
                "Rp " + df.format(harga),
                "Rp " + df.format(subtotal)
            });
        }

        // Panel total
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel lblTotal = new JLabel("TOTAL TRANSAKSI: Rp " + df.format(totalTransaksi));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotal.setForeground(new Color(0, 102, 204));
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        
        totalPanel.add(lblTotal, BorderLayout.EAST);
        footerPanel.add(totalPanel, BorderLayout.NORTH);
        
        // Garis footer
        JSeparator footerSeparator = new JSeparator();
        footerSeparator.setForeground(new Color(0, 102, 204));
        footerPanel.add(footerSeparator, BorderLayout.CENTER);
        
        // Pesan terima kasih
        JLabel lblThanks = new JLabel("Terima kasih telah berbelanja di BYNEST", JLabel.CENTER);
        lblThanks.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblThanks.setForeground(Color.GRAY);
        lblThanks.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        footerPanel.add(lblThanks, BorderLayout.SOUTH);
        
        bodyPanel.add(footerPanel, BorderLayout.SOUTH);
        panelStruk.add(bodyPanel, BorderLayout.CENTER);

        // ================= TAMPILKAN DENGAN JOPTIONPANE =================
        JOptionPane.showMessageDialog(
            this, 
            panelStruk, 
            "Detail Transaksi - " + kodeTransaksi, 
            JOptionPane.PLAIN_MESSAGE
        );

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
            this, 
            "Gagal memuat detail transaksi!\n" + e.getMessage(),
            "Error", 
            JOptionPane.ERROR_MESSAGE
        );
    }
}

    public static void main(String[] args){
        String kasirLogin = "kasir1";
        SwingUtilities.invokeLater(() -> new RiwayatTransaksi(kasirLogin).setVisible(true));
    }
}