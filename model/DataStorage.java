package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Pusat data dan logika sistem: menyimpan seluruh koleksi data (akun, divisi, anggota/pendaftaran, tugas, riwayat),
 * menyediakan operasi CRUD dan transaksi, serta menyimpan/membaca data dari file teks.
 *
 * <p>Pelanggaran aturan bisnis dilaporkan lewat {@link IllegalArgumentException} (input tidak valid),
 * {@link IllegalStateException} (kondisi tidak memungkinkan), atau {@link NoSuchElementException}
 * (data tidak ditemukan). Seluruhnya ditangkap oleh class CLI.
 */
public class DataStorage {

    /** Pemisah antar kolom pada file penyimpanan. */
    public static final String PEMISAH = "|";

    private final Path folder;
    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private final List<Divisi> divisiList = new ArrayList<>();
    private final List<Anggota> anggotaList = new ArrayList<>();
    private final List<Tugas> tugasList = new ArrayList<>();
    private final List<RiwayatAdmin> riwayatAdminList = new ArrayList<>();
    private final List<RiwayatMember> riwayatMemberList = new ArrayList<>();

    /**
     * Membuat penyimpanan data pada folder tertentu.
     *
     * @param namaFolder nama folder file data (dibuat otomatis saat menyimpan)
     */
    public DataStorage(String namaFolder) {
        this.folder = Paths.get(namaFolder);
    }

    /**
     * Membersihkan teks dari karakter yang merusak format file (pemisah dan baris baru).
     *
     * @param teks teks mentah
     * @return teks yang aman disimpan
     */
    public static String bersih(String teks) {
        if (teks == null) {
            return "";
        }
        return teks.replace(PEMISAH, "/").replace("\n", " ").replace("\r", " ");
    }

    // ======================================================================
    // AKUN
    // ======================================================================

    /**
     * Memverifikasi username dan password.
     *
     * @param username username yang diinput
     * @param password password yang diinput
     * @return akun yang berhasil login (bisa {@link Admin} atau {@link Member})
     * @throws IllegalArgumentException jika username atau password salah
     */
    public Account login(String username, String password) {
        Account akun = accounts.get(username.trim().toLowerCase());
        if (akun == null || !akun.cekPassword(password)) {
            throw new IllegalArgumentException("Username atau password salah.");
        }
        return akun;
    }

    /**
     * Mendaftarkan akun member baru.
     *
     * @param username username baru
     * @param password password baru
     * @param nama     nama lengkap
     * @param nim      nomor induk mahasiswa
     * @param jurusan  jurusan/program studi
     * @return akun member yang dibuat
     * @throws IllegalArgumentException jika data tidak valid atau username sudah dipakai
     */
    public Member registrasiMember(String username, String password, String nama, String nim, String jurusan) {
        if (username.isEmpty() || username.contains(" ") || username.contains(PEMISAH) || username.length() < 3) {
            throw new IllegalArgumentException("Username minimal 3 karakter, tanpa spasi dan tanpa '|'.");
        }
        if (password.length() < 4 || password.contains(PEMISAH)) {
            throw new IllegalArgumentException("Password minimal 4 karakter dan tidak boleh mengandung '|'.");
        }
        wajib(nama, "Nama lengkap");
        wajib(nim, "NIM");
        wajib(jurusan, "Jurusan");
        if (accounts.containsKey(username.toLowerCase())) {
            throw new IllegalArgumentException("Username '" + username + "' sudah digunakan.");
        }
        Member member = new Member(username, password, nama.trim(), nim.trim(), jurusan.trim());
        accounts.put(username.toLowerCase(), member);
        return member;
    }

    /**
     * Mengambil semua akun yang berperan sebagai member.
     *
     * @return daftar member
     */
    public List<Member> getSemuaMember() {
        List<Member> hasil = new ArrayList<>();
        for (Account akun : accounts.values()) {
            if (akun instanceof Member) {
                hasil.add((Member) akun);
            }
        }
        return hasil;
    }

    /**
     * Mencari nama lengkap dari username (dipakai untuk tampilan).
     *
     * @param username username akun
     * @return nama lengkap, atau username itu sendiri bila akun tidak ditemukan
     */
    public String namaDari(String username) {
        Account akun = accounts.get(username.toLowerCase());
        return akun == null ? username : akun.getNamaLengkap();
    }

    // ======================================================================
    // DIVISI
    // ======================================================================

    /**
     * Mengambil salinan daftar divisi.
     *
     * @return salinan daftar divisi
     */
    public List<Divisi> getDivisiList() {
        return new ArrayList<>(divisiList);
    }

    /**
     * Mencari divisi berdasarkan kode.
     *
     * @param id kode divisi (tidak membedakan huruf besar/kecil)
     * @return divisi yang ditemukan
     * @throws NoSuchElementException jika kode tidak ada
     */
    public Divisi cariDivisi(String id) {
        for (Divisi d : divisiList) {
            if (d.getId().equalsIgnoreCase(id.trim())) {
                return d;
            }
        }
        throw new NoSuchElementException("Divisi dengan kode '" + id + "' tidak ditemukan.");
    }

    /**
     * Menambah divisi baru dengan kode otomatis.
     *
     * @param admin     username admin pelaku
     * @param nama      nama divisi
     * @param deskripsi deskripsi divisi
     * @param kuota     kuota anggota
     * @return divisi yang dibuat
     * @throws IllegalArgumentException jika input tidak valid atau nama sudah ada
     */
    public Divisi tambahDivisi(String admin, String nama, String deskripsi, int kuota) {
        wajib(nama, "Nama divisi");
        if (kuota < 1) {
            throw new IllegalArgumentException("Kuota minimal 1.");
        }
        cekNamaDivisiUnik(nama, null);
        Divisi divisi = new Divisi(nextKode("D", idDivisi()), nama.trim(), deskripsi.trim(), kuota);
        divisiList.add(divisi);
        catatAdmin(admin, "TAMBAH DIVISI", divisi.getId() + " - " + divisi.getNama());
        return divisi;
    }

    /**
     * Mengubah data divisi.
     *
     * @param admin     username admin pelaku
     * @param id        kode divisi
     * @param nama      nama baru
     * @param deskripsi deskripsi baru
     * @param kuota     kuota baru
     * @throws NoSuchElementException   jika divisi tidak ada
     * @throws IllegalArgumentException jika input tidak valid atau kuota lebih kecil dari jumlah anggota
     */
    public void editDivisi(String admin, String id, String nama, String deskripsi, int kuota) {
        Divisi divisi = cariDivisi(id);
        wajib(nama, "Nama divisi");
        if (kuota < jumlahAnggota(divisi.getId()) || kuota < 1) {
            throw new IllegalArgumentException("Kuota tidak boleh lebih kecil dari jumlah anggota saat ini ("
                    + jumlahAnggota(divisi.getId()) + ").");
        }
        cekNamaDivisiUnik(nama, divisi.getId());
        divisi.setNama(nama.trim());
        divisi.setDeskripsi(deskripsi.trim());
        divisi.setKuota(kuota);
        catatAdmin(admin, "EDIT DIVISI", divisi.getId() + " - " + divisi.getNama());
    }

    /**
     * Menghapus divisi beserta seluruh anggota dan tugas di dalamnya.
     *
     * @param admin username admin pelaku
     * @param id    kode divisi
     * @throws NoSuchElementException jika divisi tidak ada
     */
    public void hapusDivisi(String admin, String id) {
        Divisi divisi = cariDivisi(id);
        divisiList.remove(divisi);
        anggotaList.removeIf(a -> a.getIdDivisi().equals(divisi.getId()));
        tugasList.removeIf(t -> t.getIdDivisi().equals(divisi.getId()));
        catatAdmin(admin, "HAPUS DIVISI", divisi.getId() + " - " + divisi.getNama());
    }

    /**
     * Menghitung anggota aktif (sudah terverifikasi) pada sebuah divisi.
     *
     * @param idDivisi kode divisi
     * @return jumlah anggota aktif
     */
    public int jumlahAnggota(String idDivisi) {
        int jumlah = 0;
        for (Anggota a : anggotaList) {
            if (a.getStatus() == Anggota.Status.AKTIF && a.getIdDivisi().equalsIgnoreCase(idDivisi)) {
                jumlah++;
            }
        }
        return jumlah;
    }

    /**
     * Mencari divisi yang namanya mengandung kata kunci.
     *
     * @param kataKunci kata kunci pencarian
     * @return divisi yang cocok
     */
    public List<Divisi> cariDivisiByNama(String kataKunci) {
        String kunci = kataKunci.trim().toLowerCase();
        return divisiList.stream()
                .filter(d -> d.getNama().toLowerCase().contains(kunci))
                .collect(Collectors.toList());
    }

    /**
     * Mengambil daftar divisi terurut dari sisa kuota terbanyak.
     *
     * @return daftar divisi terurut dari sisa kuota terbanyak
     */
    public List<Divisi> urutkanDivisiBySisaKuota() {
        List<Divisi> hasil = getDivisiList();
        hasil.sort(Comparator.comparingInt((Divisi d) -> d.getKuota() - jumlahAnggota(d.getId())).reversed());
        return hasil;
    }

   // ======================================================================
    // ANGGOTA
    // ======================================================================

    /**
     * Mengambil salinan daftar anggota aktif (yang sudah diverifikasi).
     *
     * @return salinan daftar anggota aktif (yang sudah diverifikasi)
     */
    public List<Anggota> getAnggotaList() {
        return anggotaList.stream()
                .filter(a -> a.getStatus() == Anggota.Status.AKTIF)
                .collect(Collectors.toList());
    }

    /**
     * Mengambil anggota aktif pada satu divisi.
     *
     * @param idDivisi kode divisi
     * @return daftar anggota aktif divisi tersebut
     */
    public List<Anggota> getAnggotaByDivisi(String idDivisi) {
        return anggotaList.stream()
                .filter(a -> a.getStatus() == Anggota.Status.AKTIF && a.getIdDivisi().equalsIgnoreCase(idDivisi))
                .collect(Collectors.toList());
    }

    /**
     * Mencari anggota aktif berdasarkan kode.
     *
     * @param id kode anggota
     * @return anggota yang ditemukan
     * @throws NoSuchElementException jika kode tidak ada atau anggota belum aktif
     */
    public Anggota cariAnggota(String id) {
        for (Anggota a : anggotaList) {
            if (a.getStatus() == Anggota.Status.AKTIF && a.getId().equalsIgnoreCase(id.trim())) {
                return a;
            }
        }
        throw new NoSuchElementException("Anggota dengan kode '" + id + "' tidak ditemukan.");
    }

    /**
     * Mencari data keanggotaan/pendaftaran milik seorang member, apa pun statusnya.
     *
     * @param username username member
     * @return data anggota terkait, atau {@code null} jika member belum pernah mendaftar
     */
    public Anggota cariAnggotaByUsername(String username) {
        for (Anggota a : anggotaList) {
            if (a.punyaAkun() && a.getUsername().equalsIgnoreCase(username)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Mencari data anggota aktif milik seorang member.
     *
     * @param username username member
     * @return anggota aktif terkait, atau {@code null} jika member belum resmi menjadi panitia
     */
    public Anggota cariAnggotaAktifByUsername(String username) {
        Anggota a = cariAnggotaByUsername(username);
        return (a != null && a.getStatus() == Anggota.Status.AKTIF) ? a : null;
    }

    /**
     * Menambah anggota secara manual oleh admin.
     *
     * @param admin    username admin pelaku
     * @param nama     nama anggota
     * @param jabatan  jabatan anggota
     * @param idDivisi kode divisi tujuan
     * @return anggota yang dibuat
     * @throws NoSuchElementException   jika divisi tidak ada
     * @throws IllegalArgumentException jika input kosong
     * @throws IllegalStateException    jika kuota divisi penuh
     */
    public Anggota tambahAnggota(String admin, String nama, String jabatan, String idDivisi) {
        wajib(nama, "Nama anggota");
        wajib(jabatan, "Jabatan");
        Divisi divisi = cariDivisi(idDivisi);
        cekKuota(divisi);
        Anggota anggota = new Anggota(nextKode("A", idAnggota()), nama.trim(), jabatan.trim(),
                divisi.getId(), Anggota.TANPA_AKUN);
        anggotaList.add(anggota);
        catatAdmin(admin, "TAMBAH ANGGOTA", anggota.getId() + " - " + anggota.getNama() + " ke " + divisi.getNama());
        return anggota;
    }

    /**
     * Mengubah data anggota (nama, jabatan, atau pindah divisi).
     *
     * @param admin    username admin pelaku
     * @param id       kode anggota
     * @param nama     nama baru
     * @param jabatan  jabatan baru
     * @param idDivisi kode divisi baru
     * @throws NoSuchElementException   jika anggota atau divisi tidak ada
     * @throws IllegalArgumentException jika input kosong
     * @throws IllegalStateException    jika divisi tujuan penuh
     */
    public void editAnggota(String admin, String id, String nama, String jabatan, String idDivisi) {
        Anggota anggota = cariAnggota(id);
        wajib(nama, "Nama anggota");
        wajib(jabatan, "Jabatan");
        Divisi tujuan = cariDivisi(idDivisi);
        if (!tujuan.getId().equals(anggota.getIdDivisi())) {
            cekKuota(tujuan);
            lepasTugasMilik(anggota);
            anggota.setIdDivisi(tujuan.getId());
        }
        anggota.setNama(nama.trim());
        anggota.setJabatan(jabatan.trim());
        catatAdmin(admin, "EDIT ANGGOTA", anggota.getId() + " - " + anggota.getNama());
    }

    /**
     * Menghapus anggota. Tugas yang sedang dikerjakan anggota tersebut dikembalikan ke status TERSEDIA.
     *
     * @param admin username admin pelaku
     * @param id    kode anggota
     * @throws NoSuchElementException jika anggota tidak ada
     */
    public void hapusAnggota(String admin, String id) {
        Anggota anggota = cariAnggota(id);
        lepasTugasMilik(anggota);
        anggotaList.remove(anggota);
        catatAdmin(admin, "HAPUS ANGGOTA", anggota.getId() + " - " + anggota.getNama());
    }

    /**
     * Member mendaftar menjadi panitia pada sebuah divisi. Pendaftaran berstatus MENUNGGU dan baru
     * resmi menjadi anggota setelah diverifikasi admin. Member yang pendaftarannya DITOLAK boleh mendaftar ulang.
     *
     * @param username username member
     * @param idDivisi kode divisi tujuan
     * @return data pendaftaran (berstatus MENUNGGU)
     * @throws NoSuchElementException jika divisi atau akun tidak ada
     * @throws IllegalStateException  jika sudah menjadi anggota, masih menunggu verifikasi, atau kuota penuh
     */
    public Anggota daftarPanitia(String username, String idDivisi) {
        Account akun = accounts.get(username.toLowerCase());
        if (akun == null) {
            throw new NoSuchElementException("Akun '" + username + "' tidak ditemukan.");
        }
        Anggota lama = cariAnggotaByUsername(username);
        if (lama != null && lama.getStatus() == Anggota.Status.AKTIF) {
            throw new IllegalStateException("Anda sudah menjadi anggota divisi " + cariDivisi(lama.getIdDivisi()).getNama() + ".");
        }
        if (lama != null && lama.getStatus() == Anggota.Status.MENUNGGU) {
            throw new IllegalStateException("Pendaftaran Anda ke divisi " + cariDivisi(lama.getIdDivisi()).getNama()
                    + " masih menunggu verifikasi admin.");
        }
        Divisi divisi = cariDivisi(idDivisi);
        cekKuota(divisi);
        Anggota pendaftaran = lama;
        if (lama != null) {
            lama.setIdDivisi(divisi.getId());
            lama.setStatus(Anggota.Status.MENUNGGU);
        } else {
            pendaftaran = new Anggota(nextKode("A", idAnggota()), akun.getNamaLengkap(), "Staff",
                    divisi.getId(), akun.getUsername(), Anggota.Status.MENUNGGU);
            anggotaList.add(pendaftaran);
        }
        catatMember(username, "DAFTAR PANITIA", "Mendaftar ke divisi " + divisi.getNama() + " (menunggu verifikasi admin)");
        return pendaftaran;
    }

    /**
     * Mengambil daftar pendaftaran panitia yang menunggu verifikasi admin.
     *
     * @return daftar pendaftaran panitia yang menunggu verifikasi admin
     */
    public List<Anggota> getPendaftaranMenunggu() {
        return anggotaList.stream()
                .filter(a -> a.getStatus() == Anggota.Status.MENUNGGU)
                .collect(Collectors.toList());
    }

    /**
     * Mencari pendaftaran panitia yang sedang menunggu verifikasi.
     *
     * @param id kode pendaftaran (kode anggota)
     * @return pendaftaran yang ditemukan
     * @throws NoSuchElementException jika kode tidak ada atau pendaftaran tidak sedang menunggu
     */
    public Anggota cariPendaftaran(String id) {
        for (Anggota a : anggotaList) {
            if (a.getStatus() == Anggota.Status.MENUNGGU && a.getId().equalsIgnoreCase(id.trim())) {
                return a;
            }
        }
        throw new NoSuchElementException("Pendaftaran dengan kode '" + id + "' tidak ditemukan atau sudah diverifikasi.");
    }

    /**
     * Admin memverifikasi pendaftaran panitia. Jika diterima, member resmi menjadi anggota divisi (AKTIF);
     * jika ditolak, statusnya menjadi DITOLAK dan member boleh mendaftar ulang.
     *
     * @param admin  username admin pelaku
     * @param id     kode pendaftaran (kode anggota)
     * @param terima true untuk menerima, false untuk menolak
     * @throws NoSuchElementException jika pendaftaran tidak ditemukan
     * @throws IllegalStateException  jika diterima tetapi kuota divisi sudah penuh
     */
    public void verifikasiPendaftaran(String admin, String id, boolean terima) {
        Anggota pendaftaran = cariPendaftaran(id);
        Divisi divisi = cariDivisi(pendaftaran.getIdDivisi());
        String hasil;
        if (terima) {
            cekKuota(divisi);
            pendaftaran.setStatus(Anggota.Status.AKTIF);
            hasil = "DITERIMA";
        } else {
            pendaftaran.setStatus(Anggota.Status.DITOLAK);
            hasil = "DITOLAK";
        }
        catatAdmin(admin, "VERIFIKASI PANITIA", pendaftaran.getNama() + " -> divisi " + divisi.getNama() + " " + hasil);
        catatMember(pendaftaran.getUsername(), "VERIFIKASI PANITIA",
                "Pendaftaran ke divisi " + divisi.getNama() + " " + hasil);
    }

    // ======================================================================
    // TUGAS
    // ======================================================================

    /**
     * Mengambil salinan daftar seluruh tugas.
     *
     * @return salinan daftar seluruh tugas
     */
    public List<Tugas> getTugasList() {
        return new ArrayList<>(tugasList);
    }

    /**
     * Mengambil tugas pada satu divisi.
     *
     * @param idDivisi kode divisi
     * @return daftar tugas divisi tersebut
     */
    public List<Tugas> getTugasByDivisi(String idDivisi) {
        return tugasList.stream()
                .filter(t -> t.getIdDivisi().equalsIgnoreCase(idDivisi))
                .collect(Collectors.toList());
    }

    /**
     * Mengambil tugas yang sedang/pernah dikerjakan seorang member.
     *
     * @param username username member
     * @return daftar tugas milik member
     */
    public List<Tugas> getTugasByPengambil(String username) {
        return tugasList.stream()
                .filter(t -> t.getPengambil().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    /**
     * Mengambil tugas yang menunggu verifikasi admin.
     *
     * @return tugas yang menunggu verifikasi admin
     */
    public List<Tugas> getTugasMenunggu() {
        return tugasList.stream()
                .filter(t -> t.getStatus() == Tugas.Status.MENUNGGU_VERIFIKASI)
                .collect(Collectors.toList());
    }

    /**
     * Mencari tugas berdasarkan kode.
     *
     * @param id kode tugas
     * @return tugas yang ditemukan
     * @throws NoSuchElementException jika kode tidak ada
     */
    public Tugas cariTugas(String id) {
        for (Tugas t : tugasList) {
            if (t.getId().equalsIgnoreCase(id.trim())) {
                return t;
            }
        }
        throw new NoSuchElementException("Tugas dengan kode '" + id + "' tidak ditemukan.");
    }

    /**
     * Menambah tugas baru untuk sebuah divisi.
     *
     * @param admin     username admin pelaku
     * @param judul     judul tugas
     * @param deskripsi deskripsi tugas
     * @param idDivisi  kode divisi pemilik tugas
     * @param deadline  tenggat dengan format yyyy-MM-dd
     * @return tugas yang dibuat
     * @throws NoSuchElementException   jika divisi tidak ada
     * @throws IllegalArgumentException jika input kosong atau format tanggal salah
     */
    public Tugas tambahTugas(String admin, String judul, String deskripsi, String idDivisi, String deadline) {
        wajib(judul, "Judul tugas");
        Divisi divisi = cariDivisi(idDivisi);
        Tugas tugas = new Tugas(nextKode("T", idTugas()), judul.trim(), deskripsi.trim(),
                divisi.getId(), parseTanggal(deadline));
        tugasList.add(tugas);
        catatAdmin(admin, "TAMBAH TUGAS", tugas.getId() + " - " + tugas.getJudul() + " (" + divisi.getNama() + ")");
        return tugas;
    }

    /**
     * Mengubah judul, deskripsi, dan tenggat tugas.
     *
     * @param admin     username admin pelaku
     * @param id        kode tugas
     * @param judul     judul baru
     * @param deskripsi deskripsi baru
     * @param deadline  tenggat baru (yyyy-MM-dd)
     * @throws NoSuchElementException   jika tugas tidak ada
     * @throws IllegalArgumentException jika input kosong atau format tanggal salah
     */
    public void editTugas(String admin, String id, String judul, String deskripsi, String deadline) {
        Tugas tugas = cariTugas(id);
        wajib(judul, "Judul tugas");
        tugas.setDeadline(parseTanggal(deadline));
        tugas.setJudul(judul.trim());
        tugas.setDeskripsi(deskripsi.trim());
        catatAdmin(admin, "EDIT TUGAS", tugas.getId() + " - " + tugas.getJudul());
    }

    /**
     * Menghapus tugas.
     *
     * @param admin username admin pelaku
     * @param id    kode tugas
     * @throws NoSuchElementException jika tugas tidak ada
     */
    public void hapusTugas(String admin, String id) {
        Tugas tugas = cariTugas(id);
        tugasList.remove(tugas);
        catatAdmin(admin, "HAPUS TUGAS", tugas.getId() + " - " + tugas.getJudul());
    }

    /**
     * Member (yang sudah resmi menjadi panitia) mengambil tugas yang tersedia pada divisinya.
     *
     * @param username username member
     * @param idTugas  kode tugas
     * @throws NoSuchElementException jika tugas tidak ada
     * @throws IllegalStateException  jika member belum resmi jadi panitia, tugas milik divisi lain, atau tugas sudah diambil
     */
    public void ambilTugas(String username, String idTugas) {
        Anggota anggota = cariAnggotaAktifByUsername(username);
        if (anggota == null) {
            throw new IllegalStateException("Anda belum resmi menjadi panitia divisi mana pun.");
        }
        Tugas tugas = cariTugas(idTugas);
        if (!tugas.getIdDivisi().equals(anggota.getIdDivisi())) {
            throw new IllegalStateException("Tugas ini bukan milik divisi Anda.");
        }
        if (tugas.getStatus() != Tugas.Status.TERSEDIA) {
            throw new IllegalStateException("Tugas ini sudah diambil (" + tugas.getStatus() + ").");
        }
        tugas.setStatus(Tugas.Status.DIAMBIL);
        tugas.setPengambil(username);
        catatMember(username, "AMBIL TUGAS", tugas.getId() + " - " + tugas.getJudul());
    }

    /**
     * Member mengajukan tugas yang dikerjakannya sebagai selesai (menunggu verifikasi admin).
     *
     * @param username username member
     * @param idTugas  kode tugas
     * @throws NoSuchElementException jika tugas tidak ada
     * @throws IllegalStateException  jika tugas bukan milik member atau statusnya bukan DIAMBIL
     */
    public void ajukanSelesai(String username, String idTugas) {
        Tugas tugas = cariTugas(idTugas);
        if (!tugas.getPengambil().equalsIgnoreCase(username) || tugas.getStatus() != Tugas.Status.DIAMBIL) {
            throw new IllegalStateException("Tugas ini bukan tugas Anda yang sedang dikerjakan.");
        }
        tugas.setStatus(Tugas.Status.MENUNGGU_VERIFIKASI);
        catatMember(username, "AJUKAN SELESAI", tugas.getId() + " - " + tugas.getJudul());
    }

    /**
     * Admin memverifikasi tugas yang diajukan selesai. Jika ditolak, tugas kembali ke status DIAMBIL.
     *
     * @param admin   username admin pelaku
     * @param idTugas kode tugas
     * @param terima  true untuk menerima, false untuk menolak
     * @throws NoSuchElementException jika tugas tidak ada
     * @throws IllegalStateException  jika tugas tidak sedang menunggu verifikasi
     */
    public void verifikasiTugas(String admin, String idTugas, boolean terima) {
        Tugas tugas = cariTugas(idTugas);
        if (tugas.getStatus() != Tugas.Status.MENUNGGU_VERIFIKASI) {
            throw new IllegalStateException("Tugas ini tidak sedang menunggu verifikasi.");
        }
  tugas.setStatus(terima ? Tugas.Status.SELESAI : Tugas.Status.DIAMBIL);
        String hasil = terima ? "DITERIMA" : "DITOLAK (perlu dikerjakan ulang)";
        catatAdmin(admin, "VERIFIKASI TUGAS", tugas.getId() + " - " + tugas.getJudul() + " -> " + hasil);
        catatMember(tugas.getPengambil(), "HASIL VERIFIKASI", tugas.getId() + " - " + tugas.getJudul() + " " + hasil);
    }

// ======================================================================
    // RIWAYAT
    // ======================================================================

    /**
     * Mengambil salinan seluruh riwayat aktivitas admin.
     *
     * @return salinan seluruh riwayat aktivitas admin
     */
    public List<RiwayatAdmin> getRiwayatAdmin() {
        return new ArrayList<>(riwayatAdminList);
    }

    /**
     * Mengambil salinan seluruh riwayat member (semua pengguna).
     *
     * @return salinan seluruh riwayat member (semua pengguna)
     */
    public List<RiwayatMember> getRiwayatMemberSemua() {
        return new ArrayList<>(riwayatMemberList);
    }

    /**
     * Mengambil riwayat milik satu member.
     *
     * @param username username member
     * @return daftar riwayat member tersebut
     */
    public List<RiwayatMember> getRiwayatMember(String username) {
        return riwayatMemberList.stream()
                .filter(r -> r.getUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    /**
     * Mencatat aktivitas admin ke riwayat admin.
     *
     * @param admin  username admin pelaku
     * @param aksi   jenis aksi
     * @param detail keterangan aksi
     */
    private void catatAdmin(String admin, String aksi, String detail) {
        int id = riwayatAdminList.stream().mapToInt(RiwayatAdmin::getId).max().orElse(0) + 1;
        riwayatAdminList.add(new RiwayatAdmin(id, admin, aksi, detail));
    }

    /**
     * Mencatat transaksi member ke riwayat member.
     *
     * @param username username member terkait
     * @param aksi     jenis aksi
     * @param detail   keterangan aksi
     */
    private void catatMember(String username, String aksi, String detail) {
        int id = riwayatMemberList.stream().mapToInt(RiwayatMember::getId).max().orElse(0) + 1;
        riwayatMemberList.add(new RiwayatMember(id, username, aksi, detail));
    }

    // ======================================================================
    // PENYIMPANAN FILE
    // ======================================================================

    /**
     * Membaca seluruh data dari file di folder penyimpanan, lalu menyiapkan data awal bila masih kosong
     * (akun admin bawaan dan beberapa divisi contoh).
     *
     * @return true jika semua file berhasil dibaca, false jika terjadi kesalahan baca
     */
    public boolean muat() {
        boolean berhasil = true;
        try {
            accounts.clear();
            divisiList.clear();
            anggotaList.clear();
            tugasList.clear();
            riwayatAdminList.clear();
            riwayatMemberList.clear();

            for (String[] p : bacaBaris("akun.txt")) {
                try {
                    if ("ADMIN".equals(p[0]) && p.length >= 4) {
                        accounts.put(p[1].toLowerCase(), new Admin(p[1], p[2], p[3]));
                    } else if ("MEMBER".equals(p[0]) && p.length >= 6) {
                        accounts.put(p[1].toLowerCase(), new Member(p[1], p[2], p[3], p[4], p[5]));
                    }
                } catch (RuntimeException e) {
                    // baris rusak dilewati
                }
            }
            for (String[] p : bacaBaris("divisi.txt")) {
                try {
                    divisiList.add(new Divisi(p[0], p[1], p[2], Integer.parseInt(p[3])));
                } catch (RuntimeException e) {
                    // baris rusak dilewati
                }
            }
            for (String[] p : bacaBaris("anggota.txt")) {
                try {
                    Anggota.Status status = p.length >= 6 ? Anggota.Status.valueOf(p[5]) : Anggota.Status.AKTIF;
                    anggotaList.add(new Anggota(p[0], p[1], p[2], p[3], p[4], status));
                } catch (RuntimeException e) {
                    // baris rusak dilewati
                }
            }
            for (String[] p : bacaBaris("tugas.txt")) {
                try {
                    tugasList.add(new Tugas(p[0], p[1], p[2], p[3], LocalDate.parse(p[4]),
                            Tugas.Status.valueOf(p[5]), p[6]));
                } catch (RuntimeException e) {
                    // baris rusak dilewati
                }
            }
            for (String[] p : bacaBaris("riwayat_admin.txt")) {
                try {
                    riwayatAdminList.add(new RiwayatAdmin(Integer.parseInt(p[0]), p[1], p[2], p[3],
                            LocalDateTime.parse(p[4], RiwayatAdmin.FORMAT)));
                } catch (RuntimeException e) {
                    // baris rusak dilewati
                }
            }
            for (String[] p : bacaBaris("riwayat_member.txt")) {
                try {
                    riwayatMemberList.add(new RiwayatMember(Integer.parseInt(p[0]), p[1], p[2], p[3],
                            LocalDateTime.parse(p[4], RiwayatAdmin.FORMAT)));
                } catch (RuntimeException e) {
                    // baris rusak dilewati
                }
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca data: " + e.getMessage());
            berhasil = false;
        }
        siapkanDataAwal();
        return berhasil;
    }
   /**
     * Menyimpan seluruh data ke file di folder penyimpanan.
     *
     * @return true jika berhasil, false jika terjadi kesalahan tulis
     */
    public boolean simpan() {
        try {
            Files.createDirectories(folder);
            tulis("akun.txt", accounts.values().stream().map(Account::toRecord).collect(Collectors.toList()));
            tulis("divisi.txt", divisiList.stream().map(Divisi::toRecord).collect(Collectors.toList()));
            tulis("anggota.txt", anggotaList.stream().map(Anggota::toRecord).collect(Collectors.toList()));
            tulis("tugas.txt", tugasList.stream().map(Tugas::toRecord).collect(Collectors.toList()));
            tulis("riwayat_admin.txt", riwayatAdminList.stream().map(RiwayatAdmin::toRecord).collect(Collectors.toList()));
            tulis("riwayat_member.txt", riwayatMemberList.stream().map(RiwayatMember::toRecord).collect(Collectors.toList()));
            return true;
        } catch (IOException e) {
            System.err.println("Gagal menyimpan data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Menyimpan laporan teks ke file laporan.txt di folder penyimpanan.
     *
     * @param baris isi laporan, satu elemen per baris
     * @return lokasi file laporan, atau {@code null} jika gagal menyimpan
     */
    public Path simpanLaporan(List<String> baris) {
        try {
            Files.createDirectories(folder);
            Path file = folder.resolve("laporan.txt");
            Files.write(file, baris, StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            System.err.println("Gagal menyimpan laporan: " + e.getMessage());
            return null;
        }
    }

    /**
     * Membaca file teks menjadi daftar baris yang sudah dipecah per kolom.
     *
     * @param namaFile nama file di folder penyimpanan
     * @return daftar baris (kosong jika file belum ada)
     * @throws IOException jika file gagal dibaca
     */
    private List<String[]> bacaBaris(String namaFile) throws IOException {
        List<String[]> hasil = new ArrayList<>();
        Path file = folder.resolve(namaFile);
        if (!Files.exists(file)) {
            return hasil;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String baris;
            while ((baris = reader.readLine()) != null) {
                if (!baris.isBlank()) {
                    hasil.add(baris.split("\\|", -1));
                }
            }
        }
        return hasil;
    }

    /**
     * Menulis daftar baris ke file teks (menimpa isi lama).
     *
     * @param namaFile nama file di folder penyimpanan
     * @param baris    isi file, satu elemen per baris
     * @throws IOException jika file gagal ditulis
     */
    private void tulis(String namaFile, List<String> baris) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(folder.resolve(namaFile), StandardCharsets.UTF_8)) {
            for (String b : baris) {
                writer.write(b);
                writer.newLine();
            }
        }
    }

    /** Menyiapkan akun admin bawaan dan beberapa divisi contoh jika data masih kosong. */
    private void siapkanDataAwal() {
        boolean adaAdmin = accounts.values().stream().anyMatch(a -> a instanceof Admin);
        if (!adaAdmin) {
            accounts.put("admin", new Admin("admin", "admin123", "Administrator"));
        }
        if (divisiList.isEmpty()) {
            divisiList.add(new Divisi("D01", "Acara", "Menyusun rundown dan menjalankan acara", 5));
            divisiList.add(new Divisi("D02", "Humas", "Relasi sponsor dan media partner", 4));
            divisiList.add(new Divisi("D03", "Konsumsi", "Penyediaan makanan dan minuman", 3));
        }
    }






















































































































































































































































































































































































































 
