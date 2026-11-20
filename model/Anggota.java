package model;

/**
 * Data anggota panitia pada sebuah divisi.
 * Anggota bisa ditambahkan langsung oleh admin (langsung AKTIF), atau berasal dari pendaftaran seorang
 * {@link Member} yang harus diverifikasi admin terlebih dahulu (MENUNGGU, lalu AKTIF atau DITOLAK).
 */
public class Anggota {

    /** Tahapan status keanggotaan panitia. */
    public enum Status {
        /** Pendaftaran sudah dikirim dan menunggu verifikasi admin. */
        MENUNGGU,
        /** Sudah diverifikasi dan resmi menjadi panitia divisi. */
        AKTIF,
        /** Pendaftaran ditolak admin (member boleh mendaftar ulang). */
        DITOLAK
    }

    /** Penanda untuk anggota yang tidak terhubung ke akun member. */
    public static final String TANPA_AKUN = "-";

    private final String id;
    private String nama;
    private String jabatan;
    private String idDivisi;
    private final String username;
    private Status status;

    /**
     * Membuat anggota yang langsung berstatus AKTIF (dipakai saat admin menambah anggota).
     *
     * @param id       kode unik anggota, misalnya "A01"
     * @param nama     nama anggota
     * @param jabatan  jabatan di divisi (Ketua, Sekretaris, Staff, dst.)
     * @param idDivisi kode divisi tempat anggota bertugas
     * @param username username akun member yang terhubung, atau {@link #TANPA_AKUN}
     */
    public Anggota(String id, String nama, String jabatan, String idDivisi, String username) {
        this(id, nama, jabatan, idDivisi, username, Status.AKTIF);
    }

    /**
     * Membuat anggota dengan status tertentu (dipakai untuk pendaftaran member dan saat memuat dari file).
     *
     * @param id       kode unik anggota
     * @param nama     nama anggota
     * @param jabatan  jabatan di divisi
     * @param idDivisi kode divisi tujuan
     * @param username username akun member yang terhubung, atau {@link #TANPA_AKUN}
     * @param status   status keanggotaan
     */
    public Anggota(String id, String nama, String jabatan, String idDivisi, String username, Status status) {
        this.id = id;
        this.nama = nama;
        this.jabatan = jabatan;
        this.idDivisi = idDivisi;
        this.username = username;
        this.status = status;
    }

    /**
     * Mengubah anggota menjadi satu baris teks untuk disimpan ke file.
     *
     * @return baris record anggota
     */
    public String toRecord() {
        return String.join(DataStorage.PEMISAH, id, DataStorage.bersih(nama),
                DataStorage.bersih(jabatan), idDivisi, username, status.name());
    }

    /**
     * Memeriksa apakah anggota terhubung ke akun member.
     *
     * @return true jika anggota terhubung ke akun member
     */
    public boolean punyaAkun() {
        return !TANPA_AKUN.equals(username);
    }

    /**
     * Mengambil kode anggota.
     *
     * @return kode anggota
     */
    public String getId() {
        return id;
    }

    /**
     * Mengambil nama anggota.
     *
     * @return nama anggota
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
     * Mengambil jabatan anggota.
     *
     * @return jabatan anggota
     */
    public String getJabatan() {
        return jabatan;
    }

    /**
     * Mengubah jabatan.
     *
     * @param jabatan jabatan baru
     */
    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }

    /**
     * Mengambil kode divisi anggota.
     *
     * @return kode divisi anggota
     */
    public String getIdDivisi() {
        return idDivisi;
    }

    /**
     * Mengubah kode divisi.
     *
     * @param idDivisi kode divisi baru
     */
    public void setIdDivisi(String idDivisi) {
        this.idDivisi = idDivisi;
    }

    /**
     * Mengambil username akun yang terhubung, atau {@link #TANPA_AKUN}.
     *
     * @return username akun yang terhubung, atau {@link #TANPA_AKUN}
     */
    public String getUsername() {
        return username;
    }

    /**
     * Mengambil status keanggotaan saat ini.
     *
     * @return status keanggotaan saat ini
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Mengubah status keanggotaan.
     *
     * @param status status keanggotaan baru
     */
    public void setStatus(Status status) {
        this.status = status;
    }
}
