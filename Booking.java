public class Booking {
    private String namaPemesan;
    private String jenisLapangan;
    private String tanggal;
    private SlotWaktu slot;
    private double totalHarga;
    private String tipePembayaran;
    private double jumlahBayar;
    private double sisaBayar;
    private String statusPembayaran;

    public Booking(String namaPemesan, String jenisLapangan, String tanggal, SlotWaktu slot, double hargaDasar, String tipePembayaran) {
        this.namaPemesan = namaPemesan;
        this.jenisLapangan = jenisLapangan;
        this.tanggal = tanggal;
        this.slot = slot;
        this.tipePembayaran = tipePembayaran;
        
        this.totalHarga = slot.hitungTarif(hargaDasar);
        
        if (tipePembayaran.equalsIgnoreCase("DP")) {
            this.jumlahBayar = totalHarga * 0.50; 
            this.sisaBayar = totalHarga - jumlahBayar;
            this.statusPembayaran = "DP (Kurang Rp " + String.format("%,.0f", sisaBayar) + ")";
        } else {
            this.jumlahBayar = totalHarga;
            this.sisaBayar = 0;
            this.statusPembayaran = "LUNAS";
        }
    }

    public String getNamaPemesan() { return namaPemesan; }
    public String getJenisLapangan() { return jenisLapangan; }
    public String getTanggal() { return tanggal; }
    public SlotWaktu getSlot() { return slot; }
    public double getTotalHarga() { return totalHarga; }
    public String getTipePembayaran() { return tipePembayaran; }
    public double getJumlahBayar() { return jumlahBayar; }
    public double getSisaBayar() { return sisaBayar; }
    public String getStatusPembayaran() { return statusPembayaran; }
}
