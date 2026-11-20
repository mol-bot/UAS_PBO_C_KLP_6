package model;

/** Akun pengelola kepanitiaan: mengelola divisi, anggota, tugas, dan memverifikasi hasil tugas. */
public class Admin extends Account {

    /**
     * Membuat akun admin.
     *
     * @param username    username admin
     * @param password    password admin
     * @param namaLengkap nama lengkap admin
     */
    public Admin(String username, String password, String namaLengkap) {
        super(username, password, namaLengkap);
    }

    /**
     * Menyebutkan peran akun ini.
     *
     * @return teks "ADMIN"
     */
    @Override
    public String getRole() {
        return "ADMIN";
    }

    /**
     * Memberikan ringkasan akun admin.
     *
     * @return ringkasan hak akses admin
     */
    @Override
    public String getInfo() {
        return "Admin - dapat mengelola divisi, anggota, tugas, serta memverifikasi pendaftaran dan tugas.";
    }

    /**
     * Mengubah akun admin menjadi satu baris teks untuk disimpan ke file.
     *
     * @return baris record akun
     */
    @Override
    public String toRecord() {
        return recordDasar();
    }
}
