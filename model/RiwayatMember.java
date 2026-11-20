package model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Catatan transaksi seorang member (bergabung divisi, mengambil tugas, mengajukan selesai, hasil verifikasi). */
public class RiwayatMember {

    private final int id;
    private final String username;
    private final String aksi;
    private final String detail;
    private final LocalDateTime waktu;

    /**
     * Membuat catatan baru dengan waktu saat ini.
     *
     * @param id       nomor riwayat
     * @param username username member terkait
     * @param aksi     jenis aksi, misalnya "AMBIL TUGAS"
     * @param detail   keterangan aksi
     */
    public RiwayatMember(int id, String username, String aksi, String detail) {
        this(id, username, aksi, detail, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    /**
     * Membuat catatan lengkap (dipakai saat memuat dari file).
     *
     * @param id       nomor riwayat
     * @param username username member terkait
     * @param aksi     jenis aksi
     * @param detail   keterangan aksi
     * @param waktu    waktu aksi terjadi
     */
    public RiwayatMember(int id, String username, String aksi, String detail, LocalDateTime waktu) {
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
                DataStorage.bersih(detail), waktu.format(RiwayatAdmin.FORMAT));
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
     * Mengambil username member terkait.
     *
     * @return username member terkait
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
