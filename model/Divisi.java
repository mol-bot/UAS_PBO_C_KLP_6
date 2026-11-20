package model;

/** Divisi dalam kepanitiaan (misalnya Acara, Humas, Konsumsi) beserta kuota anggotanya. */
public class Divisi {

    private final String id;
    private String nama;
    private String deskripsi;
    private int kuota;

    /**
     * Membuat divisi.
     *
     * @param id        kode unik divisi, misalnya "D01"
     * @param nama      nama divisi
     * @param deskripsi penjelasan singkat tugas divisi
     * @param kuota     jumlah anggota maksimal
     */
    public Divisi(String id, String nama, String deskripsi, int kuota) {
        this.id = id;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.kuota = kuota;
    }

    /**
     * Mengubah divisi menjadi satu baris teks untuk disimpan ke file.
     *
     * @return baris record divisi
     */
    public String toRecord() {
        return String.join(DataStorage.PEMISAH, id, DataStorage.bersih(nama),
                DataStorage.bersih(deskripsi), String.valueOf(kuota));
    }

    /**
     * Mengambil kode divisi.
     *
     * @return kode divisi
     */
    public String getId() {
        return id;
    }

    /**
     * Mengambil nama divisi.
     *
     * @return nama divisi
     */
    public String getNama() {
        return nama;
    }

    /**
     * Mengubah nama.
     *
     * @param nama nama baru
     */
    public void setNama(String nama) {
        this.nama = nama;
    }

    /**
     * Mengambil deskripsi divisi.
     *
     * @return deskripsi divisi
     */
    public String getDeskripsi() {
        return deskripsi;
    }

    /**
     * Mengubah deskripsi.
     *
     * @param deskripsi deskripsi baru
     */
    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    /**
     * Mengambil kuota maksimal anggota.
     *
     * @return kuota maksimal anggota
     */
    public int getKuota() {
        return kuota;
    }

    /**
     * Mengubah kuota (validasi dilakukan oleh {@link DataStorage}).
     *
     * @param kuota kuota baru (validasi dilakukan oleh {@link DataStorage})
     */
    public void setKuota(int kuota) {
        this.kuota = kuota;
    }
}
