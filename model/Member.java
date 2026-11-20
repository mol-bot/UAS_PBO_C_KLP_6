package model;

/** Akun pengguna biasa (calon/anggota panitia) yang dapat bergabung ke divisi dan mengerjakan tugas. */
public class Member extends Account {

    private String nim;
    private String jurusan;

    /**
     * Membuat akun member.
     *
     * @param username    username member
     * @param password    password member
     * @param namaLengkap nama lengkap member
     * @param nim         nomor induk mahasiswa
     * @param jurusan     jurusan/program studi
     */
    public Member(String username, String password, String namaLengkap, String nim, String jurusan) {
        super(username, password, namaLengkap);
        this.nim = nim;
        this.jurusan = jurusan;
    }

    /**
     * Menyebutkan peran akun ini.
     *
     * @return teks "MEMBER"
     */
    @Override
    public String getRole() {
        return "MEMBER";
    }

    /**
     * Memberikan ringkasan akun member.
     *
     * @return ringkasan NIM dan jurusan member
     */
    @Override
    public String getInfo() {
        return "Member - NIM " + nim + ", jurusan " + jurusan + ".";
    }

    /**
     * Mengubah akun member menjadi satu baris teks untuk disimpan ke file.
     *
     * @return baris record akun
     */
    @Override
    public String toRecord() {
        return String.join(DataStorage.PEMISAH, recordDasar(),
                DataStorage.bersih(nim), DataStorage.bersih(jurusan));
    }

    /**
     * Mengambil NIM member.
     *
     * @return NIM member
     */
    public String getNim() {
        return nim;
    }

    /**
     * Mengubah NIM.
     *
     * @param nim NIM baru
     */
    public void setNim(String nim) {
        this.nim = nim;
    }

    /**
     * Mengambil jurusan member.
     *
     * @return jurusan member
     */
    public String getJurusan() {
        return jurusan;
    }

    /**
     * Mengubah jurusan.
     *
     * @param jurusan jurusan baru
     */
    public void setJurusan(String jurusan) {
        this.jurusan = jurusan;
    }
}
