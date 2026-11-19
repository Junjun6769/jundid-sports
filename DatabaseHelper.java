import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/db_booking_lapangan";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS bookings (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "nama_pemesan VARCHAR(100) NOT NULL, " +
                "jenis_lapangan VARCHAR(50) NOT NULL, " +
                "tanggal DATE NOT NULL, " +
                "jam_slot VARCHAR(150) NOT NULL, " +
                "hari_booking VARCHAR(50) NOT NULL, " +
                "total_harga DOUBLE NOT NULL, " +
                "tipe_pembayaran VARCHAR(20) NOT NULL, " +
                "jumlah_bayar DOUBLE NOT NULL, " +
                "sisa_bayar DOUBLE NOT NULL, " +
                "status_pembayaran VARCHAR(50) NOT NULL);";
                
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Gagal inisialisasi tabel: " + e.getMessage());
        }
    }

    public static boolean simpanBooking(Booking b) {
        String sql = "INSERT INTO bookings(nama_pemesan, jenis_lapangan, tanggal, jam_slot, hari_booking, total_harga, tipe_pembayaran, jumlah_bayar, sisa_bayar, status_pembayaran) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, b.getNamaPemesan());
            pstmt.setString(2, b.getJenisLapangan());
            pstmt.setString(3, b.getTanggal());
            pstmt.setString(4, b.getSlot().getJam());
            pstmt.setString(5, b.getSlot().getHari());
            pstmt.setDouble(6, b.getTotalHarga());
            pstmt.setString(7, b.getTipePembayaran());
            pstmt.setDouble(8, b.getJumlahBayar());
            pstmt.setDouble(9, b.getSisaBayar());
            pstmt.setString(10, b.getStatusPembayaran());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method baru untuk melunasi transaksi DP berdasarkan ID
    public static boolean lunasiBooking(int id) {
        String sql = "UPDATE bookings SET tipe_pembayaran = 'Full Payment', jumlah_bayar = total_harga, sisa_bayar = 0, status_pembayaran = 'LUNAS' WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getBookedSlots(String tanggal, String jenisLapangan, String hariBooking) {
        List<String> bookedSlots = new ArrayList<>();
        String sql = "SELECT jam_slot FROM bookings WHERE tanggal = ? AND jenis_lapangan = ? AND hari_booking = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tanggal);
            pstmt.setString(2, jenisLapangan);
            pstmt.setString(3, hariBooking);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String rawJam = rs.getString("jam_slot");
                String[] ranges = rawJam.split(",\\s*");
                for (String range : ranges) {
                    String[] parts = range.split(" - ");
                    if (parts.length == 2) {
                        int jamMulai = Integer.parseInt(parts[0].substring(0, 2));
                        int jamSelesai = Integer.parseInt(parts[1].substring(0, 2));
                        for (int h = jamMulai; h < jamSelesai; h++) {
                            bookedSlots.add(String.format("%02d:00 - %02d:00", h, h + 1));
                        }
                    } else {
                        bookedSlots.add(range);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookedSlots;
    }
}