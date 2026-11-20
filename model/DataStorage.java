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
    