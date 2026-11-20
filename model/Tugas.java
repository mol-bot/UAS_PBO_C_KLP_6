package model;

import java.time.LocalDate;

/**
 * Tugas yang dibuat admin untuk sebuah divisi. Member anggota divisi tersebut dapat mengambil
 * dan menyelesaikan tugas, lalu admin memverifikasi hasilnya.
 */
public class Tugas {

    /** Tahapan status sebuah tugas. */
    public enum Status {
        /** Belum diambil siapa pun. */
        TERSEDIA,
        /** Sedang dikerjakan seorang member. */
        DIAMBIL,
        /** Sudah diajukan selesai, menunggu verifikasi admin. */
        MENUNGGU_VERIFIKASI,
        /** Sudah diverifikasi admin. */
        SELESAI
    }

    /** Penanda tugas yang belum punya pengambil. */
    public static final String BELUM_DIAMBIL = "-";

    private final String id;
    private String judul;
    private String deskripsi;
    private final String idDivisi;
    private LocalDate deadline;
    private Status status;
    private String pengambil;

    /**
     * Membuat tugas baru berstatus TERSEDIA.
     *
     * @param id        kode unik tugas, misalnya "T01"
     * @param judul     judul tugas
     * @param deskripsi penjelasan tugas
     * @param idDivisi  kode divisi pemilik tugas
     * @param deadline  tenggat tugas
     */
    public Tugas(String id, String judul, String deskripsi, String idDivisi, LocalDate deadline) {
        this(id, judul, deskripsi, idDivisi, deadline, Status.TERSEDIA, BELUM_DIAMBIL);
    }

    /**
     * Membuat tugas lengkap (dipakai saat memuat dari file).
     *
     * @param id        kode unik tugas
     * @param judul     judul tugas
     * @param deskripsi penjelasan tugas
     * @param idDivisi  kode divisi pemilik tugas
     * @param deadline  tenggat tugas
     * @param status    status tugas
     * @param pengambil username member pengambil, atau {@link #BELUM_DIAMBIL}
     */
    public Tugas(String id, String judul, String deskripsi, String idDivisi, LocalDate deadline,
                 Status status, String pengambil) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.idDivisi = idDivisi;
        this.deadline = deadline;
        this.status = status;
        this.pengambil = pengambil;
    }

    /**
     * Mengubah tugas menjadi satu baris teks untuk disimpan ke file.
     *
     * @return baris record tugas
     */
    public String toRecord() {
        return String.join(DataStorage.PEMISAH, id, DataStorage.bersih(judul), DataStorage.bersih(deskripsi),
                idDivisi, deadline.toString(), status.name(), pengambil);
    }

    /** Mengembalikan tugas ke status TERSEDIA tanpa pengambil. */
    public void lepaskan() {
        status = Status.TERSEDIA;
        pengambil = BELUM_DIAMBIL;
    }

    /**
     * Mengambil kode tugas.
     *
     * @return kode tugas
     */
    public String getId() {
        return id;
    }

    /**
     * Mengambil judul tugas.
     *
     * @return judul tugas
     */
    public String getJudul() {
        return judul;
    }

    /**
     * Mengubah judul.
     *
     * @param judul judul baru
     */
    public void setJudul(String judul) {
        this.judul = judul;
    }

    /**
     * Mengambil deskripsi tugas.
     *
     * @return deskripsi tugas
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
     * Mengambil kode divisi pemilik tugas.
     *
     * @return kode divisi pemilik tugas
     */
    public String getIdDivisi() {
        return idDivisi;
    }

    /**
     * Mengambil tenggat tugas.
     *
     * @return tenggat tugas
     */
    public LocalDate getDeadline() {
        return deadline;
    }

    /**
     * Mengubah tenggat.
     *
     * @param deadline tenggat baru
     */
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    /**
     * Mengambil status tugas saat ini.
     *
     * @return status tugas saat ini
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Mengubah status.
     *
     * @param status status baru
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Mengambil username pengambil tugas, atau {@link #BELUM_DIAMBIL}.
     *
     * @return username pengambil tugas, atau {@link #BELUM_DIAMBIL}
     */
    public String getPengambil() {
        return pengambil;
    }

    /**
     * Mengubah username pengambil.
     *
     * @param pengambil username pengambil baru
     */
    public void setPengambil(String pengambil) {
        this.pengambil = pengambil;
    }
}
