package cli;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import model.Account;
import model.Admin;
import model.DataStorage;
import model.Divisi;
import model.Member;
import model.Tugas;

/**
 * Menu awal program (login dan registrasi member). Class ini juga menyediakan utilitas tampilan dan
 * pembacaan input yang dipakai bersama oleh {@link AdminCli} dan {@link MemberCli}.
 */
public class LoginCli {

    private static final Scanner INPUT = new Scanner(System.in);
    private static final boolean WARNA = System.getenv("NO_COLOR") == null;
    private static final int LEBAR = 78;

    private static final String RESET = "\u001B[0m";
    private static final String MERAH = "\u001B[31m";
    private static final String HIJAU = "\u001B[32m";
    private static final String KUNING = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String TEBAL = "\u001B[1m";

    private final DataStorage storage;

    /**
     * Membuat menu login.
     *
     * @param storage pusat data aplikasi
     */
    public LoginCli(DataStorage storage) {
        this.storage = storage;
    }

    /** Menjalankan perulangan menu awal sampai pengguna memilih keluar. */
    public void jalankan() {
        while (true) {
            judul("SISTEM MANAJEMEN KEPANITIAAN");
            menu("1", "Login");
            menu("2", "Registrasi Akun Member");
            menu("0", "Keluar");
            int pilihan = bacaInt("Pilih menu: ");
            try {
                switch (pilihan) {
                    case 1:
                        login();
                        break;
                    case 2:
                        registrasi();
                        break;
                    case 0:
                        info("Terima kasih. Sampai jumpa!");
                        return;
                    default:
                        error("Pilihan tidak tersedia.");
                }
            } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
                error(e.getMessage());
            }
        }
    }

    /**
     * Memproses login lalu membuka menu sesuai jenis akun.
     *
     * @throws IllegalArgumentException jika username/password salah
     */
    private void login() {
        String username = bacaString("Username: ");
        String password = bacaString("Password: ");
        Account akun = storage.login(username, password);
        sukses("Selamat datang, " + akun.getNamaLengkap() + "!");
        info(akun.getInfo()); // Polymorphism: isi teks bergantung pada jenis akun (Admin/Member)
        // Polymorphism: menu yang dibuka ditentukan oleh jenis akun
        if (akun instanceof Admin) {
            new AdminCli(storage, (Admin) akun).jalankan();
        } else {
            new MemberCli(storage, (Member) akun).jalankan();
        }
    }

    /**
     * Memproses registrasi akun member baru.
     *
     * @throws IllegalArgumentException jika data tidak valid atau username sudah dipakai
     */
    private void registrasi() {
        String nama = bacaString("Nama lengkap : ");
        String nim = bacaString("NIM          : ");
        String jurusan = bacaString("Jurusan      : ");
        String username = bacaString("Username     : ");
        String password = bacaString("Password     : ");
        storage.registrasiMember(username, password, nama, nim, jurusan);
        simpan(storage);
        sukses("Registrasi berhasil. Silakan login.");
    }

    // ======================================================================
    // UTILITAS BERSAMA (dipakai AdminCli dan MemberCli)
    // ======================================================================

    /**
     * Membungkus teks dengan kode warna ANSI (jika warna aktif).
     *
     * @param kode  kode warna ANSI
     * @param teks  teks yang diwarnai
     * @return teks berwarna, atau teks asli bila warna dimatikan
     */
    private static String warna(String kode, String teks) {
        return WARNA ? kode + teks + RESET : teks;
    }

    /** Mencetak garis pemisah. */
    public static void garis() {
        System.out.println(warna(CYAN, "-".repeat(LEBAR)));
    }

    /**
     * Mencetak judul dalam kotak border.
     *
     * @param teks judul yang ditampilkan
     */
    public static void judul(String teks) {
        int sisa = LEBAR - 2 - teks.length();
        int kiri = Math.max(0, sisa / 2);
        int kanan = Math.max(0, sisa - kiri);
        System.out.println();
        System.out.println(warna(CYAN, "+" + "=".repeat(LEBAR - 2) + "+"));
        System.out.println(warna(CYAN, "|") + " ".repeat(kiri) + warna(TEBAL, teks) + " ".repeat(kanan) + warna(CYAN, "|"));
        System.out.println(warna(CYAN, "+" + "=".repeat(LEBAR - 2) + "+"));
    }

    /**
     * Mencetak satu baris pilihan menu.
     *
     * @param nomor nomor pilihan
     * @param teks  nama menu
     */
    public static void menu(String nomor, String teks) {
        System.out.println("  " + warna(KUNING, "[" + nomor + "]") + " " + teks);
    }

    /**
     * Mencetak pesan keberhasilan.
     *
     * @param teks pesan keberhasilan
     */
    public static void sukses(String teks) {
        System.out.println(warna(HIJAU, "[OK] " + teks));
    }

    /**
     * Mencetak pesan kesalahan.
     *
     * @param teks pesan kesalahan
     */
    public static void error(String teks) {
        System.out.println(warna(MERAH, "[!] " + teks));
    }

    /**
     * Mencetak pesan informasi.
     *
     * @param teks pesan informasi
     */
    public static void info(String teks) {
        System.out.println(warna(CYAN, "[i] " + teks));
    }

    /**
     * Membaca satu baris teks dari keyboard.
     *
     * @param prompt teks petunjuk
     * @return teks yang diketik (sudah di-trim)
     */
    public static String bacaString(String prompt) {
        System.out.print(prompt);
        try {
            return INPUT.nextLine().trim();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Input ditutup. Program berhenti.");
            System.exit(0);
            return "";
        }
    }

    /**
     * Membaca bilangan bulat; mengulang sampai input valid.
     *
     * @param prompt teks petunjuk
     * @return bilangan yang diinput
     */
    public static int bacaInt(String prompt) {
        while (true) {
            String input = bacaString(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                error("Input harus berupa angka bulat.");
            }
        }
    }

    /**
     * Meminta konfirmasi ya/tidak.
     *
     * @param prompt pertanyaan konfirmasi
     * @return true jika pengguna menjawab y/ya
     */
    public static boolean konfirmasi(String prompt) {
        String jawab = bacaString(prompt + " (y/n): ").toLowerCase();
        return jawab.equals("y") || jawab.equals("ya");
    }

    /** Menunggu pengguna menekan Enter. */
    public static void tekanEnter() {
        bacaString("\nTekan Enter untuk lanjut...");
    }

    /**
     * Memotong teks yang terlalu panjang agar muat di kolom tabel.
     *
     * @param teks    teks asli
     * @param panjang panjang maksimal
     * @return teks yang dipotong (diakhiri "..." bila terpotong)
     */
    public static String potong(String teks, int panjang) {
        if (teks == null) {
            return "";
        }
        return teks.length() <= panjang ? teks : teks.substring(0, panjang - 3) + "...";
    }

    /**
     * Menyimpan data ke file dan menampilkan peringatan bila gagal.
     *
     * @param storage pusat data aplikasi
     */
    public static void simpan(DataStorage storage) {
        if (!storage.simpan()) {
            error("Data gagal disimpan ke file.");
        }
    }

    /**
     * Mencetak tabel divisi lengkap dengan jumlah anggota dan sisa kuota.
     *
     * @param storage pusat data aplikasi
     * @param data    divisi yang ditampilkan
     */
    public static void tabelDivisi(DataStorage storage, List<Divisi> data) {
        if (data.isEmpty()) {
            info("Tidak ada data divisi.");
            return;
        }
        garis();
        System.out.printf("%-5s %-18s %-6s %-7s %-5s %s%n", "ID", "Divisi", "Kuota", "Anggota", "Sisa", "Deskripsi");
        garis();
        for (Divisi d : data) {
            int anggota = storage.jumlahAnggota(d.getId());
            System.out.printf("%-5s %-18s %-6d %-7d %-5d %s%n", d.getId(), potong(d.getNama(), 18),
                    d.getKuota(), anggota, d.getKuota() - anggota, potong(d.getDeskripsi(), 34));
        }
        garis();
    }

    /**
     * Mencetak tabel tugas.
     *
     * @param storage pusat data aplikasi
     * @param data    tugas yang ditampilkan
     */
    public static void tabelTugas(DataStorage storage, List<Tugas> data) {
        if (data.isEmpty()) {
            info("Tidak ada data tugas.");
            return;
        }
        garis();
        System.out.printf("%-5s %-22s %-6s %-11s %-20s %s%n", "ID", "Judul", "Divisi", "Deadline", "Status", "Pengambil");
        garis();
        for (Tugas t : data) {
            String pengambil = Tugas.BELUM_DIAMBIL.equals(t.getPengambil()) ? "-" : storage.namaDari(t.getPengambil());
            System.out.printf("%-5s %-22s %-6s %-11s %-20s %s%n", t.getId(), potong(t.getJudul(), 22),
                    t.getIdDivisi(), t.getDeadline(), t.getStatus(), potong(pengambil, 16));
        }
        garis();
    }
}
