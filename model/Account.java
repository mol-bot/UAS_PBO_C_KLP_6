package model;

/**
 * Abstract class induk semua akun pada sistem ({@link Admin} dan {@link Member}).
 * Atribut dienkapsulasi (private) dan diakses lewat getter/setter.
 */
public abstract class Account {

    private String username;
    private String password;
    private String namaLengkap;

    /**
     * Membuat akun baru.
     *
     * @param username    username unik untuk login
     * @param password    password akun
     * @param namaLengkap nama lengkap pemilik akun
     */
    protected Account(String username, String password, String namaLengkap) {
        this.username = username;
        this.password = password;
        this.namaLengkap = namaLengkap;
    }

    /**
     * Menyebutkan peran akun. Dioverride oleh subclass (polymorphism).
     *
     * @return teks peran, misalnya "ADMIN" atau "MEMBER"
     */
    public abstract String getRole();

    /**
     * Memberikan ringkasan singkat akun untuk ditampilkan setelah login. Dioverride oleh subclass (polymorphism).
     *
     * @return teks ringkasan akun
     */
    public abstract String getInfo();

    /**
     * Mengubah akun menjadi satu baris teks untuk disimpan ke file.
     *
     * @return baris record akun
     */
    public abstract String toRecord();

    /**
     * Memeriksa kecocokan password.
     *
     * @param input password yang diketik pengguna
     * @return true jika cocok
     */
    public boolean cekPassword(String input) {
        return password.equals(input);
    }

    /**
     * Menyusun kolom dasar (role, username, password, nama) untuk record file.
     *
     * @return potongan record yang dipakai bersama oleh subclass
     */
    protected String recordDasar() {
        return String.join(DataStorage.PEMISAH, getRole(), username, password,
                DataStorage.bersih(namaLengkap));
    }

    /**
     * Mengambil username akun.
     *
     * @return username akun
     */
    public String getUsername() {
        return username;
    }

    /**
     * Mengubah username.
     *
     * @param username username baru
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Mengambil password akun.
     *
     * @return password akun
     */
    public String getPassword() {
        return password;
    }

    /**
     * Mengubah password.
     *
     * @param password password baru
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Mengambil nama lengkap pemilik akun.
     *
     * @return nama lengkap pemilik akun
     */
    public String getNamaLengkap() {
        return namaLengkap;
    }

    /**
     * Mengubah nama lengkap.
     *
     * @param namaLengkap nama lengkap baru
     */
    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    /**
     * Menampilkan akun sebagai teks "nama (ROLE)".
     *
     * @return teks representasi akun
     */
    @Override
    public String toString() {
        return namaLengkap + " (" + getRole() + ")";
    }
}
