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
                GradientPaint gp = new GradientPaint(0, 0, new Color(0,102,204), 0, getHeight(), Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(10,10));
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
        } catch(Exception e){
            lblHeader = new JLabel("Transaksi Penjualan", JLabel.CENTER);
        }
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // ================= PANEL TENGAH =================
        JPanel centerPanel = new JPanel(new GridLayout(1,2,10,10));
        centerPanel.setOpaque(false);

        // ===== PANEL PRODUK =====
        JPanel panelProduk = new JPanel(new BorderLayout(5,5));
        panelProduk.setOpaque(false);

        txtCari = new JTextField();
        JButton btnCari = new JButton("🔍 Cari");
        btnCari.setBackground(new Color(0,153,255));
        btnCari.setForeground(Color.WHITE);
        btnCari.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.add(txtCari, BorderLayout.CENTER);
        searchPanel.add(btnCari, BorderLayout.EAST);
        panelProduk.add(searchPanel, BorderLayout.NORTH);

        modelProduk = new DefaultTableModel(new String[]{"Kode Produk","Nama","Kategori","Harga","Stok","Foto"},0){
            @Override
            public boolean isCellEditable(int row, int col){ return false; }
            @Override
            public Class<?> getColumnClass(int col){
                return col==5 ? ImageIcon.class : Object.class;
            }
        };
        tableProduk = new JTable(modelProduk);
        tableProduk.setRowHeight(60);
        JScrollPane spProduk = new JScrollPane(tableProduk);
        panelProduk.add(spProduk, BorderLayout.CENTER);

        JButton btnTambahKeranjang = new JButton("➕ Tambah ke Keranjang");
        btnTambahKeranjang.setBackground(new Color(0,204,102));
        btnTambahKeranjang.setForeground(Color.WHITE);
        btnTambahKeranjang.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel bottomPanelProduk = new JPanel(new BorderLayout());
        bottomPanelProduk.setOpaque(false);
        bottomPanelProduk.setBorder(BorderFactory.createEmptyBorder(0,0,36,0));
        bottomPanelProduk.add(btnTambahKeranjang, BorderLayout.CENTER);
        panelProduk.add(bottomPanelProduk, BorderLayout.SOUTH);

        // ===== PANEL KERANJANG =====
        JPanel panelKeranjang = new JPanel(new BorderLayout(5,5));
        panelKeranjang.setOpaque(false);

        modelKeranjang = new DefaultTableModel(new String[]{"Kode Produk","Nama","Harga","Jumlah","Subtotal","Foto"},0){
            @Override
            public boolean isCellEditable(int row, int col){ return false; }
            @Override
            public Class<?> getColumnClass(int col){
                return col==5 ? ImageIcon.class : Object.class;
            }
        };
        tableKeranjang = new JTable(modelKeranjang);
        tableKeranjang.setRowHeight(60);
        JScrollPane spKeranjang = new JScrollPane(tableKeranjang);
        panelKeranjang.add(spKeranjang, BorderLayout.CENTER);

        JPanel bottomKeranjang = new JPanel(new GridLayout(4,2,10,10));
        bottomKeranjang.setOpaque(false);
        txtTotal = new JTextField("Rp 0"); txtTotal.setEditable(false);
        txtBayar = new JTextField();

        JButton btnBayar = new JButton("💵 Bayar");
        btnBayar.setBackground(new Color(0,102,204));
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnHapus = new JButton("❌ Batal");
        btnHapus.setBackground(new Color(255,77,77));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnBack = new JButton("⬅️ Kembali");
        btnBack.setBackground(new Color(153,153,153));
        btnBack.setForeground(Color.WHITE);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));

        bottomKeranjang.add(new JLabel("Total:")); bottomKeranjang.add(txtTotal);
        bottomKeranjang.add(new JLabel("Bayar:")); bottomKeranjang.add(txtBayar);
        bottomKeranjang.add(btnBayar); bottomKeranjang.add(btnHapus);
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
            public void insertUpdate(DocumentEvent e) { formatBayar(); }
            public void removeUpdate(DocumentEvent e) { formatBayar(); }
            public void changedUpdate(DocumentEvent e) { formatBayar(); }

            private void formatBayar() {
                if (editing) return;
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
            public void insertUpdate(DocumentEvent e){ loadProduk(txtCari.getText()); }
            public void removeUpdate(DocumentEvent e){ loadProduk(txtCari.getText()); }
            public void changedUpdate(DocumentEvent e){ loadProduk(txtCari.getText()); }
        });
    }

    private void loadProduk(String keyword){
        modelProduk.setRowCount(0);
        try(Connection conn = Database.getConnection()){
            String sql = "SELECT * FROM produk WHERE nama_produk LIKE ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%"+keyword+"%");
            ResultSet rs = pst.executeQuery();
            while(rs.next()){
                int id = rs.getInt("id");
                String kodeProduk = generateKodeProduk(id);
                double harga = rs.getDouble("harga");
                ImageIcon icon = null;
                try {
                    String path = rs.getString("foto");
                    if (path != null && !path.isEmpty()) {
                        Image img = new ImageIcon(path).getImage()
                                .getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(img);
                    }
                } catch (Exception ex) {
                    System.out.println("Gagal load gambar: " + ex.getMessage());
                }

                modelProduk.addRow(new Object[]{
                        kodeProduk,
                        rs.getString("nama_produk"),
                        rs.getString("kategori"),
                        "Rp " + df.format(harga),
                        rs.getInt("stok"),
                        icon
                });
            }
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Gagal load produk!");
        }
    }

    private String generateKodeProduk(int id){
        return String.format("PRO-%04d", id);
    }

    private int getIdFromKode(String kode){
        try(Connection conn = Database.getConnection()){
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT id FROM produk");
            while(rs.next()){
                int id = rs.getInt("id");
                if(generateKodeProduk(id).equals(kode)) return id;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    private void tambahKeranjang(){
        int row = tableProduk.getSelectedRow();
        if(row==-1){ JOptionPane.showMessageDialog(this,"Pilih produk dulu!"); return; }

        String kode = (String) modelProduk.getValueAt(row,0);
        String nama = (String) modelProduk.getValueAt(row,1);
        String hargaStr = modelProduk.getValueAt(row,3).toString().replace("Rp","").replace(".","").trim();
        double harga = Double.parseDouble(hargaStr);
        int stok = (int) modelProduk.getValueAt(row,4);
        ImageIcon foto = (ImageIcon) modelProduk.getValueAt(row,5);

        String jumlahStr = JOptionPane.showInputDialog(this,"Masukkan jumlah:");
        if(jumlahStr==null) return;
        int jumlah = Integer.parseInt(jumlahStr);
        if(jumlah>stok){ JOptionPane.showMessageDialog(this,"Stok tidak cukup!"); return; }

        double subtotal = harga*jumlah;
        modelKeranjang.addRow(new Object[]{kode,nama,"Rp "+df.format(harga),jumlah,"Rp "+df.format(subtotal),foto});
        hitungTotal();
    }

    private void hitungTotal(){
        double total=0;
        for(int i=0;i<modelKeranjang.getRowCount();i++){
            String subStr = modelKeranjang.getValueAt(i,4).toString().replace("Rp","").replace(".","").trim();
            total += Double.parseDouble(subStr);
        }
        txtTotal.setText("Rp " + df.format(total));
    }

    private void bayar() {
        try {
            double total = Double.parseDouble(txtTotal.getText().replace("Rp","").replace(".","").trim());
            double bayar = Double.parseDouble(txtBayar.getText().replace("Rp","").replace(".","").trim());
            if (bayar < total) {
                JOptionPane.showMessageDialog(this, "Uang tidak cukup!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double kembalian = bayar - total;

            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false);

                int idKasir = 0;
                String sqlKasir = "SELECT id FROM users WHERE username = ?";
                PreparedStatement pstKasir = conn.prepareStatement(sqlKasir);
                pstKasir.setString(1, kasirUsername);
                ResultSet rsKasir = pstKasir.executeQuery();
                if (rsKasir.next()) idKasir = rsKasir.getInt("id");

                String sqlTrans = "INSERT INTO transaksi (kasir_id, tanggal, total, status) VALUES (?, NOW(), ?, ?)";
                PreparedStatement pstTrans = conn.prepareStatement(sqlTrans, Statement.RETURN_GENERATED_KEYS);
                pstTrans.setInt(1, idKasir);
                pstTrans.setDouble(2, total);
                pstTrans.setString(3, "selesai");
                pstTrans.executeUpdate();

                ResultSet gk = pstTrans.getGeneratedKeys();
                int idTransaksi = 0;
                if (gk.next()) idTransaksi = gk.getInt(1);

                for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                    String kodeProduk = (String) modelKeranjang.getValueAt(i, 0);
                    int idProduk = getIdFromKode(kodeProduk);
                    int qty = (int) modelKeranjang.getValueAt(i, 3);
                    String hargaStr = modelKeranjang.getValueAt(i, 2).toString().replace("Rp","").replace(".","").trim();
                    double harga = Double.parseDouble(hargaStr);

                    String sqlDet = "INSERT INTO detail_transaksi (transaksi_id, produk_id, qty, harga) VALUES (?,?,?,?)";
                    PreparedStatement pstDet = conn.prepareStatement(sqlDet);
                    pstDet.setInt(1, idTransaksi);
                    pstDet.setInt(2, idProduk);
                    pstDet.setInt(3, qty);
                    pstDet.setDouble(4, harga);
                    pstDet.executeUpdate();

                    String sqlUpdateStok = "UPDATE produk SET stok = stok - ? WHERE id = ?";
                    PreparedStatement pstStok = conn.prepareStatement(sqlUpdateStok);
                    pstStok.setInt(1, qty);
                    pstStok.setInt(2, idProduk);
                    pstStok.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Transaksi berhasil!\nID Transaksi: TRX-" + String.format("%03d", idTransaksi) +
                                "\nTotal: Rp " + df.format(total) +
                                "\nBayar: Rp " + df.format(bayar) +
                                "\nKembalian: Rp " + df.format(kembalian),
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);

                cetakStruk(idTransaksi, total, bayar, kembalian);

                modelKeranjang.setRowCount(0);
                txtTotal.setText("Rp 0");
                txtBayar.setText("");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal melakukan transaksi: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Masukkan nominal yang valid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
public void cetakStruk(int idTransaksi, double total, double bayar, double kembalian) {
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
        sb.append(String.format("ID Transaksi : TRX-%03d\n", idTransaksi));
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

        // ✅ TOMBOL OK
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
}
