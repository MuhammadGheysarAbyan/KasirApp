import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class KelolaKategoriDialog extends JDialog {
    private JTable table;
    private DefaultTableModel model;

    public KelolaKategoriDialog(JFrame parent) {
        super(parent, "Kelola Kategori", true);
        setSize(400, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(new String[]{"Kategori"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadKategori();

        JPanel panelBtn = new JPanel(new FlowLayout());
        JButton btnTambah = new JButton("Tambah");
        JButton btnEdit = new JButton("Edit");
        JButton btnHapus = new JButton("Hapus");

        panelBtn.add(btnTambah);
        panelBtn.add(btnEdit);
        panelBtn.add(btnHapus);

        add(panelBtn, BorderLayout.SOUTH);

        // ACTION: Tambah Kategori
        btnTambah.addActionListener(e -> tambahKategori());

        // ACTION: Edit Kategori
        btnEdit.addActionListener(e -> editKategori());

        // ACTION: Hapus Kategori
        btnHapus.addActionListener(e -> hapusKategori());
    }

    // LOAD KATEGORI
    private void loadKategori() {
        model.setRowCount(0);
        try (Connection conn = Database.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT DISTINCT kategori FROM produk ORDER BY kategori ASC");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("kategori")});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TAMBAH KATEGORI
    private void tambahKategori() {
        String nama = JOptionPane.showInputDialog(this, "Nama kategori baru:");
        if (nama == null || nama.trim().isEmpty()) return;

        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO produk (kategori) VALUES (?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, nama.trim());
            pst.executeUpdate();
        } catch (Exception e) {
            // abaikan kalau kategori tidak punya data produk
        }
        loadKategori();
    }

    // EDIT KATEGORI
    private void editKategori() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kategori dulu!");
            return;
        }

        String oldName = model.getValueAt(row, 0).toString();
        String newName = JOptionPane.showInputDialog(this, "Ganti nama kategori:", oldName);

        if (newName == null || newName.trim().isEmpty()) return;

        try (Connection conn = Database.getConnection()) {
            PreparedStatement pst = conn.prepareStatement(
                "UPDATE produk SET kategori=? WHERE kategori=?");
            pst.setString(1, newName.trim());
            pst.setString(2, oldName);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Kategori berhasil diubah!");
            loadKategori();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal edit kategori!\n" + e.getMessage());
        }
    }

    // HAPUS KATEGORI
    private void hapusKategori() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih kategori dulu!");
            return;
        }

        String kategori = model.getValueAt(row, 0).toString();

        try (Connection conn = Database.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM produk WHERE kategori='" + kategori + "'");

            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                    "Kategori masih digunakan produk!\nTidak bisa dihapus.",
                    "Tidak Bisa Hapus",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Yakin hapus kategori?", "Konfirmasi", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                PreparedStatement pst = conn.prepareStatement(
                    "DELETE FROM kategori WHERE nama=?");
                pst.setString(1, kategori);
                pst.executeUpdate();
                loadKategori();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal hapus kategori!\n" + e.getMessage());
        }
    }
}
