/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package penjualantiketpesawat;

import controllers.DatabaseHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class PenjualanTiketPesawat extends JFrame {
    private JTable tabelTiket;
    private DefaultTableModel model;
    private JTextField fieldMaskapai, fieldTujuan, fieldHarga, fieldJadwal;
    private JButton btnTambah;

    public PenjualanTiketPesawat() {
        setTitle("Aplikasi Penjualan Tiket Pesawat");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel untuk input data
        JPanel panelInput = new JPanel();
        panelInput.setLayout(new BoxLayout(panelInput, BoxLayout.Y_AXIS));

        JLabel labelMaskapai = new JLabel("Maskapai:");
        fieldMaskapai = new JTextField();
        JLabel labelTujuan = new JLabel("Tujuan:");
        fieldTujuan = new JTextField();
        JLabel labelHarga = new JLabel("Harga Tiket:");
        fieldHarga = new JTextField();
        JLabel labelJadwal = new JLabel("Jadwal (YYYY-MM-DD):");
        fieldJadwal = new JTextField();

        panelInput.add(labelMaskapai);
        panelInput.add(fieldMaskapai);
        panelInput.add(labelTujuan);
        panelInput.add(fieldTujuan);
        panelInput.add(labelHarga);
        panelInput.add(fieldHarga);
        panelInput.add(labelJadwal);
        panelInput.add(fieldJadwal);

        // Tombol Tambah
        btnTambah = new JButton("Tambah");
        panelInput.add(btnTambah);

        // Tabel untuk daftar tiket
        String[] kolom = {"ID", "Maskapai", "Tujuan", "Harga", "Jadwal", "Edit", "Delete"};
        model = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6; // Hanya kolom "Edit" dan "Delete" yang bisa diklik
            }
        };

        tabelTiket = new JTable(model);
        tabelTiket.getColumn("Edit").setCellRenderer(new ButtonRenderer("Edit"));
        tabelTiket.getColumn("Delete").setCellRenderer(new ButtonRenderer("Delete"));
        tabelTiket.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), this, "Edit"));
        tabelTiket.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), this, "Delete"));
        JScrollPane scrollPane = new JScrollPane(tabelTiket);

        // Layout utama
        setLayout(new BorderLayout());
        add(panelInput, BorderLayout.NORTH); // Panel input di bagian atas
        add(scrollPane, BorderLayout.CENTER); // Tabel di bagian tengah

        // Muat data awal
        loadData();

        // Event listener untuk tombol tambah
        btnTambah.addActionListener(e -> tambahTiket());
    }

    private void loadData() {
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tiket_pesawat")) {

            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("maskapai"),
                        rs.getString("tujuan"),
                        rs.getBigDecimal("harga"),
                        rs.getDate("jadwal"),
                        "Edit",
                        "Delete"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data!");
        }
    }

    private void tambahTiket() {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO tiket_pesawat (maskapai, tujuan, harga, jadwal) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, fieldMaskapai.getText());
            stmt.setString(2, fieldTujuan.getText());
            stmt.setBigDecimal(3, new java.math.BigDecimal(fieldHarga.getText()));
            stmt.setDate(4, java.sql.Date.valueOf(fieldJadwal.getText()));

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan!");
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menambahkan data!");
        }
    }

    private void editTiket(int id) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE tiket_pesawat SET maskapai = ?, tujuan = ?, harga = ?, jadwal = ? WHERE id = ?")) {

            String maskapai = JOptionPane.showInputDialog(this, "Maskapai:");
            String tujuan = JOptionPane.showInputDialog(this, "Tujuan:");
            String harga = JOptionPane.showInputDialog(this, "Harga Tiket:");
            String jadwal = JOptionPane.showInputDialog(this, "Jadwal (YYYY-MM-DD):");

            stmt.setString(1, maskapai);
            stmt.setString(2, tujuan);
            stmt.setBigDecimal(3, new java.math.BigDecimal(harga));
            stmt.setDate(4, java.sql.Date.valueOf(jadwal));
            stmt.setInt(5, id);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!");
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data!");
        }
    }

    private void hapusTiket(int id) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM tiket_pesawat WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menghapus data!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PenjualanTiketPesawat().setVisible(true));
    }

    // Renderer untuk tombol
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String label) {
            setText(label);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Editor untuk tombol
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final PenjualanTiketPesawat app;
        private final String action;

        public ButtonEditor(JCheckBox checkBox, PenjualanTiketPesawat app, String action) {
            super(checkBox);
            this.app = app;
            this.action = action;
            button = new JButton();
            button.addActionListener(e -> {
                int row = tabelTiket.getSelectedRow();
                int id = (int) tabelTiket.getValueAt(row, 0);

                if (action.equals("Edit")) {
                    app.editTiket(id);
                } else if (action.equals("Delete")) {
                    app.hapusTiket(id);
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            button.setText(action);
            return button;
        }
    }
}
