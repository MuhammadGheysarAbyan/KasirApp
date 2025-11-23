import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KelolaKategoriDialog extends JDialog {
    private JTable table;
    private DefaultTableModel model;
    private Integer selectedId = null;
    private ManajemenProduk parent;

    public KelolaKategoriDialog(ManajemenProduk parent) {
        super(parent, "Kelola Kategori", true);
        this.parent = parent;
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Model tabel
        model = new DefaultTableModel(new String[]{"ID", "Nama Kategori"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        
        // Sembunyikan kolom ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Panel tombol
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnTambah = new JButton("Tambah");
        JButton btnEdit = new JButton("Edit");
        JButton btnHapus = new JButton("Hapus");
        JButton btnTutup = new JButton("Tutup");

        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnTutup);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load data
        loadData();

        // Event handlers
        btnTambah.addActionListener(e -> tambahKategori());
        btnEdit.addActionListener(e -> editKategori());
        btnHapus.addActionListener(e -> hapusKategori());
        btnTutup.addActionListener(e -> dispose());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedId = (Integer) model.getValueAt(row, 0);
                }
            }
        });
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, nama_kategori FROM kategori ORDER BY nama_kategori";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_kategori")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data kategori!");
        }
    }

    private void tambahKategori() {
        String nama = JOptionPane.showInputDialog(this, "Masukkan nama kategori:");
        if (nama != null && !nama.trim().isEmpty()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "INSERT INTO kategori (nama_kategori) VALUES (?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, nama.trim());
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Kategori berhasil ditambahkan!");
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menambah kategori!");
            }
        }
    }

    private void editKategori() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Pilih kategori yang akan diedit!");
            return;
        }

        String namaLama = (String) model.getValueAt(table.getSelectedRow(), 1);
        String namaBaru = JOptionPane.showInputDialog(this, "Edit nama kategori:", namaLama);
        
        if (namaBaru != null && !namaBaru.trim().isEmpty()) {
            try (Connection conn = Database.getConnection()) {
                String sql = "UPDATE kategori SET nama_kategori = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, namaBaru.trim());
                pstmt.setInt(2, selectedId);
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Kategori berhasil diupdate!");
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal mengupdate kategori!");
            }
        }
    }

    private void hapusKategori() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Pilih kategori yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus kategori ini?\nProduk dengan kategori ini akan menjadi tanpa kategori.",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Database.getConnection()) {
                // Update produk yang menggunakan kategori ini menjadi tanpa kategori
                String updateSql = "UPDATE produk SET kategori_id = NULL WHERE kategori_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, selectedId);
                updateStmt.executeUpdate();

                // Hapus kategori
                String deleteSql = "DELETE FROM kategori WHERE id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, selectedId);
                deleteStmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Kategori berhasil dihapus!");
                loadData();
                selectedId = null;
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menghapus kategori!");
            }
        }
    }
}