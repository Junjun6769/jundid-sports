import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SistemBookingLapanganApp extends JFrame {

    // Palet Warna 
    private final Color COLOR_PRIMARY = Color.decode("#5B7B7A");
    private final Color COLOR_BACKGROUND = Color.decode("#E0F2E9");
    private final Color COLOR_CARD = new Color(255, 255, 255);
    private final Color COLOR_SUCCESS = Color.decode("#3C887E");
    private final Color COLOR_DANGER = Color.decode("#A17C6B");
    private final Color COLOR_WARNING = Color.decode("#CEB5A7");
    private final Color COLOR_BORDER = new Color(195, 218, 207);
    
    private final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private JTextField txtNama;
    private JComboBox<String> cbLapangan, cbNomorLapangan, cbTipePembayaran;
    private JButton btnPilihHari;
    private String hariDipilih = "Senin"; 
    
    private JPanel gridPanel;
    private JButton[] slotButtons;
    private boolean[] isSlotBooked;
    
    private String[] slotJam = {
        "08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00",
        "12:00 - 13:00", "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00",
        "16:00 - 17:00", "17:00 - 18:00", "18:00 - 19:00", "19:00 - 20:00",
        "20:00 - 21:00", "21:00 - 22:00", "22:00 - 23:00", "23:00 - 24:00"
    };
    
    private List<String> selectedJams = new ArrayList<>();
    private List<Integer> bookingIds = new ArrayList<>();
    
    private JTable tabelRekap;
    private DefaultTableModel tableModel;
    private JLabel lblTotalPendapatan;
    private JLabel lblDate; 

    public SistemBookingLapanganApp() {
        setTitle("JunDid Sports - Sistem Manajemen Lapangan");
        setSize(1150, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        DatabaseHelper.initDatabase();
        initModernUI();
        refreshGridStatus();
        loadRekapHarian();
    }

    private void initModernUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30)); 
        
        JLabel lblTitle = new JLabel("JunDid Sports - Sistem Manajemen Lapangan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        
        lblDate = new JLabel();
        lblDate.setFont(FONT_MAIN);
        lblDate.setForeground(Color.WHITE);
        headerPanel.add(lblDate, BorderLayout.EAST);
        startRealTimeClock(); 
        
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContent.setBackground(COLOR_BACKGROUND);
        mainContent.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel leftColumn = new JPanel(new BorderLayout(0, 20));
        leftColumn.setBackground(COLOR_BACKGROUND);

        JPanel formCard = createCardPanel("Formulir Pemesanan");
        JPanel formContent = new JPanel(new GridLayout(5, 2, 10, 12));
        formContent.setBackground(COLOR_CARD);
        
        formContent.add(createLabel("Nama Pemesan:"));
        txtNama = new JTextField();
        txtNama.setFont(FONT_MAIN);
        formContent.add(txtNama);

        formContent.add(createLabel("Jenis Olahraga:"));
        cbLapangan = new JComboBox<>(new String[]{"Futsal (Rp 120.000/jam)", "Badminton (Rp 50.000/jam)"});
        cbLapangan.setFont(FONT_MAIN);
        cbLapangan.setBackground(Color.WHITE);
        cbLapangan.addActionListener(e -> {
            if (cbLapangan.getSelectedIndex() != -1) {
                refreshGridStatus();
            }
        });
        formContent.add(cbLapangan);

        formContent.add(createLabel("Pilih Lapangan:"));
        cbNomorLapangan = new JComboBox<>(new String[]{"Lapangan A", "Lapangan B"});
        cbNomorLapangan.setFont(FONT_MAIN);
        cbNomorLapangan.setBackground(Color.WHITE);
        cbNomorLapangan.addActionListener(e -> refreshGridStatus());
        formContent.add(cbNomorLapangan);

        formContent.add(createLabel("Pilih Hari:"));
        btnPilihHari = new JButton(hariDipilih + " (Klik untuk Ubah)");
        btnPilihHari.setFont(FONT_MAIN);
        btnPilihHari.setBackground(Color.WHITE);
        btnPilihHari.setForeground(new Color(45, 55, 55));
        btnPilihHari.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        btnPilihHari.setFocusPainted(false);
        btnPilihHari.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPilihHari.addActionListener(e -> showPilihHariDialog());
        formContent.add(btnPilihHari);

        formContent.add(createLabel("Sistem Pembayaran:"));
        cbTipePembayaran = new JComboBox<>(new String[]{"Full Payment", "DP (50%)"});
        cbTipePembayaran.setFont(FONT_MAIN);
        cbTipePembayaran.setBackground(Color.WHITE);
        formContent.add(cbTipePembayaran);

        formCard.add(formContent, BorderLayout.CENTER);
        leftColumn.add(formCard, BorderLayout.NORTH);

        JPanel gridCard = createCardPanel("Pilih Slot Waktu");
        
        gridPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        gridPanel.setBackground(COLOR_CARD);
        slotButtons = new JButton[slotJam.length];
        isSlotBooked = new boolean[slotJam.length];

        for (int i = 0; i < slotJam.length; i++) {
            final int index = i;
            String jam = slotJam[i];
            JButton btn = new JButton(jam);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
            styleFlatButton(btn, COLOR_SUCCESS); 
            btn.addActionListener(e -> selectSlot(index, jam, btn));
            slotButtons[i] = btn;
            gridPanel.add(btn);
        }
        gridCard.add(gridPanel, BorderLayout.CENTER);

        JButton btnSubmit = new JButton("KONFIRMASI BOOKING");
        styleFlatButton(btnSubmit, COLOR_PRIMARY);
        btnSubmit.setPreferredSize(new Dimension(100, 45));
        btnSubmit.addActionListener(e -> prosesPemesanan());
        gridCard.add(btnSubmit, BorderLayout.SOUTH);

        leftColumn.add(gridCard, BorderLayout.CENTER);
        mainContent.add(leftColumn);

        JPanel rightColumn = createCardPanel("Rekap Transaksi");
        
        tableModel = new DefaultTableModel(new String[]{
            "Nama", "Lapangan", "Slot", "Hari", "Status Bayar"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelRekap = new JTable(tableModel);
        tabelRekap.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelRekap.setRowHeight(30);
        tabelRekap.setShowVerticalLines(false);
        tabelRekap.setSelectionBackground(COLOR_BACKGROUND); 
        tabelRekap.setSelectionForeground(new Color(30, 45, 45));

        JTableHeader tableHeader = tabelRekap.getTableHeader();
        tableHeader.setBackground(COLOR_BACKGROUND);
        tableHeader.setForeground(new Color(45, 65, 64));
        tableHeader.setFont(FONT_BOLD);
        tableHeader.setPreferredSize(new Dimension(100, 35));

        JScrollPane scrollPane = new JScrollPane(tabelRekap);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        rightColumn.add(scrollPane, BorderLayout.CENTER);

        JPanel panelFooter = new JPanel(new BorderLayout());
        panelFooter.setBackground(COLOR_CARD);
        panelFooter.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnLunasi = new JButton("Lunasi DP Terpilih");
        styleFlatButton(btnLunasi, COLOR_SUCCESS);
        btnLunasi.setPreferredSize(new Dimension(150, 38));
        btnLunasi.addActionListener(e -> prosesPelunasanDP());
        panelFooter.add(btnLunasi, BorderLayout.WEST);
        
        lblTotalPendapatan = new JLabel("Total Pendapatan: Rp 0");
        lblTotalPendapatan.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTotalPendapatan.setForeground(COLOR_PRIMARY);
        panelFooter.add(lblTotalPendapatan, BorderLayout.EAST);
        
        rightColumn.add(panelFooter, BorderLayout.SOUTH);
        mainContent.add(rightColumn);

        add(mainContent, BorderLayout.CENTER);
    }

    private void prosesPelunasanDP() {
        int selectedRow = tabelRekap.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Pilih salah satu baris transaksi DP pada tabel terlebih dahulu!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String statusSaatIni = (String) tableModel.getValueAt(selectedRow, 4);
        if (statusSaatIni.equalsIgnoreCase("LUNAS")) {
            JOptionPane.showMessageDialog(this,
                "Transaksi ini sudah berstatus LUNAS!",
                "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int idBooking = bookingIds.get(selectedRow);
        String namaPemesan = (String) tableModel.getValueAt(selectedRow, 0);
        String lapangan = (String) tableModel.getValueAt(selectedRow, 1);
        String slot = (String) tableModel.getValueAt(selectedRow, 2);

        int konfirmasi = JOptionPane.showConfirmDialog(this,
            "Konfirmasi pelunasan pembayaran untuk:\n" +
            "Nama: " + namaPemesan + "\n" +
            "Lapangan: " + lapangan + " (" + slot + ")\n" +
            "Status saat ini: " + statusSaatIni + "\n\n" +
            "Ubah status menjadi LUNAS?",
            "Konfirmasi Pelunasan DP", JOptionPane.YES_NO_OPTION);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            if (DatabaseHelper.lunasiBooking(idBooking)) {
                JOptionPane.showMessageDialog(this,
                    "Pelunasan berhasil! Status transaksi sekarang LUNAS.",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadRekapHarian();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal memperbarui status pelunasan di database!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getNamaLapanganLengkap() {
        String olahraga = cbLapangan.getSelectedIndex() == 0 ? "Futsal" : "Badminton";
        String nomor = (String) cbNomorLapangan.getSelectedItem();
        return olahraga + " - " + nomor;
    }

    private void updateHargaLapanganUI() {
        int currentIndex = cbLapangan.getSelectedIndex();
        if (currentIndex == -1) currentIndex = 0;

        boolean isWeekend = hariDipilih.equals("Sabtu") || hariDipilih.equals("Minggu");
        cbLapangan.removeAllItems();

        if (isWeekend) {
            cbLapangan.addItem("Futsal (Rp 144.000/jam - Weekend)");
            cbLapangan.addItem("Badminton (Rp 60.000/jam - Weekend)");
        } else {
            cbLapangan.addItem("Futsal (Rp 120.000/jam)");
            cbLapangan.addItem("Badminton (Rp 50.000/jam)");
        }

        cbLapangan.setSelectedIndex(currentIndex);
    }

    private void showPilihHariDialog() {
        JDialog dialog = new JDialog(this, "Pilih Hari Booking", true);
        dialog.setSize(450, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panelDialog = new JPanel(new GridLayout(2, 4, 10, 10));
        panelDialog.setBackground(COLOR_BACKGROUND);
        panelDialog.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] hariArray = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};
        
        for (String h : hariArray) {
            JButton btnHari = new JButton(h);
            if (h.equals("Sabtu") || h.equals("Minggu")) {
                styleFlatButton(btnHari, COLOR_WARNING);
                btnHari.setForeground(new Color(45, 35, 30)); 
            } else {
                styleFlatButton(btnHari, COLOR_PRIMARY);
            }
            btnHari.addActionListener(e -> {
                hariDipilih = h; 
                btnPilihHari.setText(h + " (Klik untuk Ubah)");
                updateHargaLapanganUI(); 
                refreshGridStatus();
                dialog.dispose(); 
            });
            panelDialog.add(btnHari);
        }
        dialog.add(panelDialog);
        dialog.setVisible(true); 
    }

    private void startRealTimeClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy  HH:mm:ss");
            lblDate.setText("Waktu: " + sdf.format(new Date()));
        });
        timer.start();
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(45, 65, 64));
        card.add(lblTitle, BorderLayout.NORTH);
        return card;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        label.setForeground(new Color(65, 85, 84));
        return label;
    }

    private void styleFlatButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); 
        btn.setBorderPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private String getTanggalHariIni() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private void refreshGridStatus() {
        String lapanganLengkap = getNamaLapanganLengkap();
        List<String> booked = DatabaseHelper.getBookedSlots(getTanggalHariIni(), lapanganLengkap, hariDipilih);

        selectedJams.clear();

        for (int i = 0; i < slotJam.length; i++) {
            String jam = slotJam[i];
            if (booked.contains(jam)) {
                isSlotBooked[i] = true;
                slotButtons[i].setBackground(COLOR_DANGER); 
                slotButtons[i].setForeground(Color.WHITE);
                slotButtons[i].setText("Terisi");
                slotButtons[i].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } else {
                isSlotBooked[i] = false;
                slotButtons[i].setBackground(COLOR_SUCCESS); 
                slotButtons[i].setForeground(Color.WHITE);
                slotButtons[i].setText(jam);
                slotButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        }
    }

    private void selectSlot(int index, String jam, JButton btn) {
        if (isSlotBooked[index]) {
            return;
        }

        if (selectedJams.contains(jam)) {
            selectedJams.remove(jam);
            btn.setBackground(COLOR_SUCCESS); 
            btn.setForeground(Color.WHITE);
        } else {
            selectedJams.add(jam);
            btn.setBackground(COLOR_WARNING); 
            btn.setForeground(new Color(45, 35, 30));
        }
    }

    private String formatGabunganJam(List<String> listJam) {
        List<String> sorted = new ArrayList<>(listJam);
        Collections.sort(sorted);

        List<String> hasilGabungan = new ArrayList<>();
        String startAwal = "";
        String endTerakhir = "";

        for (String slot : sorted) {
            String[] parts = slot.split(" - ");
            String start = parts[0];
            String end = parts[1];

            if (startAwal.isEmpty()) {
                startAwal = start;
                endTerakhir = end;
            } else if (start.equals(endTerakhir)) {
                endTerakhir = end;
            } else {
                hasilGabungan.add(startAwal + " - " + endTerakhir);
                startAwal = start;
                endTerakhir = end;
            }
        }
        if (!startAwal.isEmpty()) {
            hasilGabungan.add(startAwal + " - " + endTerakhir);
        }

        return String.join(", ", hasilGabungan);
    }

    private void prosesPemesanan() {
        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama pemesan tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedJams.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan pilih minimal 1 slot waktu dari grid!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String lapanganLengkap = getNamaLapanganLengkap();
        double hargaDasar = cbLapangan.getSelectedIndex() == 0 ? 120000 : 50000;
        String tipeBayar = cbTipePembayaran.getSelectedIndex() == 0 ? "Full Payment" : "DP";

        int totalJam = selectedJams.size();
        String jamTergabung = formatGabunganJam(selectedJams);
        boolean isWeekend = hariDipilih.equals("Sabtu") || hariDipilih.equals("Minggu");

        SlotWaktu slotGabungan = isWeekend
                ? new WeekendSlot(hariDipilih, jamTergabung, totalJam)
                : new WeekdaySlot(hariDipilih, jamTergabung, totalJam);

        Booking booking = new Booking(nama, lapanganLengkap, getTanggalHariIni(), slotGabungan, hargaDasar, tipeBayar);

        if (DatabaseHelper.simpanBooking(booking)) {
            JOptionPane.showMessageDialog(this,
                "Booking Berhasil (" + totalJam + " Jam)!\n" +
                "Lapangan: " + lapanganLengkap + "\n" +
                "Jadwal: " + jamTergabung + "\n" +
                "Total Tarif: Rp " + String.format("%,.0f", booking.getTotalHarga()) + "\n" +
                "Total Dibayar: Rp " + String.format("%,.0f", booking.getJumlahBayar()) + "\n" +
                "Status: " + booking.getStatusPembayaran(),
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            txtNama.setText("");
            refreshGridStatus();
            loadRekapHarian();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi ke database!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRekapHarian() {
        tableModel.setRowCount(0);
        bookingIds.clear();
        double totalPendapatan = 0;
        String sql = "SELECT * FROM bookings WHERE tanggal = ?";
        
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, getTanggalHariIni());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookingIds.add(rs.getInt("id"));
                String lapTampil = rs.getString("jenis_lapangan").replace("Lapangan ", "");
                
                tableModel.addRow(new Object[]{
                    rs.getString("nama_pemesan"),
                    lapTampil, 
                    rs.getString("jam_slot"),
                    rs.getString("hari_booking"),
                    rs.getString("status_pembayaran")
                });
                totalPendapatan += rs.getDouble("jumlah_bayar");
            }
            lblTotalPendapatan.setText("Total Pendapatan: Rp " + String.format("%,.0f", totalPendapatan));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new SistemBookingLapanganApp().setVisible(true));
    }
}