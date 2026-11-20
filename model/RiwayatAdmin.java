package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Catatan aktivitas yang dilakukan admin (tambah/ubah/hapus data dan verifikasi tugas). */
public class RiwayatAdmin {

    /** Format tanggal-waktu yang dipakai untuk tampilan dan file. */
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int id;
    private final String username;
    private final String aksi;
    private final String detail;
    private final LocalDateTime waktu;

    /**
     * Membuat catatan baru dengan waktu saat ini.
     *
     * @param id       nomor riwayat
     * @param username username admin pelaku
     * @param aksi     jenis aksi, misalnya "TAMBAH DIVISI"
     * @param detail   keterangan aksi
     */
    public RiwayatAdmin(int id, String username, String aksi, String detail) {
        this(id, username, aksi, detail, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    /**
     * Membuat catatan lengkap (dipakai saat memuat dari file).
     *
     * @param id       nomor riwayat
     * @param username username admin pelaku
     * @param aksi     jenis aksi
     * @param detail   keterangan aksi
     * @param waktu    waktu aksi dilakukan
     */
    public RiwayatAdmin(int id, String username, String aksi, String detail, LocalDateTime waktu) {
        this.id = id;
        this.username = username;
        this.aksi = aksi;
        this.detail = detail;
        this.waktu = waktu;
    }

    /**
     * Mengubah catatan menjadi satu baris teks untuk disimpan ke file.
     *
     * @return baris record riwayat
     */
    public String toRecord() {
        return String.join(DataStorage.PEMISAH, String.valueOf(id), username, DataStorage.bersih(aksi),
                DataStorage.bersih(detail), waktu.format(FORMAT));
    }

    /**
     * Mengambil nomor riwayat.
     *
     * @return nomor riwayat
     */
    public int getId() {
        return id;
    }

    /**
     * Mengambil username admin pelaku.
     *
     * @return username admin pelaku
     */
    public String getUsername() {
        return username;
    }

    /**
     * Mengambil jenis aksi.
     *
     * @return jenis aksi
     */
    public String getAksi() {
        return aksi;
    }

    /**
     * Mengambil keterangan aksi.
     *
     * @return keterangan aksi
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Mengambil waktu aksi.
     *
     * @return waktu aksi
     */
    public LocalDateTime getWaktu() {
        return waktu;
    }
}
