public abstract class SlotWaktu {
    private String hari;
    private String jam;
    private int durasiJam;

    public SlotWaktu(String hari, String jam, int durasiJam) {
        this.hari = hari;
        this.jam = jam;
        this.durasiJam = durasiJam;
    }

    public String getHari() { return hari; }
    public String getJam() { return jam; }
    public int getDurasiJam() { return durasiJam; }

    public abstract double hitungTarif(double hargaDasar);
}

class WeekdaySlot extends SlotWaktu {
    public WeekdaySlot(String hari, String jam, int durasiJam) {
        super(hari, jam, durasiJam);
    }

    @Override
    public double hitungTarif(double hargaDasar) {
        return hargaDasar * getDurasiJam();
    }
}

class WeekendSlot extends SlotWaktu {
    public WeekendSlot(String hari, String jam, int durasiJam) {
        super(hari, jam, durasiJam);
    }

    @Override
    public double hitungTarif(double hargaDasar) {
        return (hargaDasar * getDurasiJam()) * 1.20;
    }
}