package cli;

import model.Admin;
import model.Anggota;
import model.DataStorage;
import model.Divisi;
import model.Member;
import model.RiwayatAdmin;
import model.RiwayatMember;
import model.Tugas;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Menu untuk admin: kelola divisi, anggota, dan tugas; verifikasi pendaftaran panitia dan tugas;
 * lihat member, riwayat, dan laporan.
 */
public class AdminCli {

    private final DataStorage storage;
    private final Admin admin;

    /**
     * Membuat menu admin.
     *
     * @param storage pusat data aplikasi
     * @param admin   akun admin yang sedang login
     */
    public AdminCli(DataStorage storage, Admin admin) {
        this.storage = storage;
        this.admin = admin;
    }

    /** Menjalankan perulangan menu admin sampai logout. */
    public void jalankan() {
        while (true) {
            LoginCli.judul("MENU ADMIN - " + admin.getNamaLengkap());
            LoginCli.menu("1", "Kelola Divisi");
            LoginCli.menu("2", "Kelola Anggota");
            LoginCli.menu("3", "Kelola Tugas");
            LoginCli.menu("4", "Verifikasi Pendaftaran Panitia");
            LoginCli.menu("5", "Verifikasi Tugas");
            LoginCli.menu("6", "Daftar Member");
            LoginCli.menu("7", "Riwayat Aktivitas Admin");
            LoginCli.menu("8", "Riwayat Transaksi Member");
            LoginCli.menu("9", "Laporan Rekap");
            LoginCli.menu("0", "Logout");
            int pilihan = LoginCli.bacaInt("Pilih menu: ");
            try {
                switch (pilihan) {
                    case 1:
                        menuDivisi();
                        break;
                    case 2:
                        menuAnggota();
                        break;
                    case 3:
                        menuTugas();
                        break;
                    case 4:
                        verifikasiPendaftaran();
                        break;
                    case 5:
                        verifikasiTugas();
                        break;
                    case 6:
                        daftarMember();
                        break;
                    case 7:
                        riwayatAdmin();
                        break;
                    case 8:
                        riwayatMember();
                        break;
                    case 9:
                        laporanRekap();
                        break;
                    case 0:
                        return;
                    default:
                        LoginCli.error("Pilihan tidak tersedia.");
                }
            } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
                LoginCli.error(e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- DIVISI

    /** Menampilkan submenu kelola divisi (lihat, tambah, edit, hapus, urutkan). */
    private void menuDivisi() {
        while (true) {
            LoginCli.judul("KELOLA DIVISI");
            LoginCli.menu("1", "Lihat semua divisi");
            LoginCli.menu("2", "Tambah divisi");
            LoginCli.menu("3", "Edit divisi");
            LoginCli.menu("4", "Hapus divisi");
            LoginCli.menu("5", "Urutkan divisi (sisa kuota terbanyak)");
            LoginCli.menu("0", "Kembali");
            int pilihan = LoginCli.bacaInt("Pilih menu: ");
            try {
                switch (pilihan) {
                    case 1:
                        LoginCli.tabelDivisi(storage, storage.getDivisiList());
                        break;
                    case 2:
                        tambahDivisi();
                        break;
                    case 3:
                        editDivisi();
                        break;
                    case 4:
                        hapusDivisi();
                        break;
                    case 5:
                        LoginCli.info("Diurutkan dari sisa kuota terbanyak.");
                        LoginCli.tabelDivisi(storage, storage.urutkanDivisiBySisaKuota());
                        break;
                    case 0:
                        return;
                    default:
                        LoginCli.error("Pilihan tidak tersedia.");
                }
            } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
                LoginCli.error(e.getMessage());
            }
        }
    }

    /** Meminta data divisi baru lalu menyimpannya. */
    private void tambahDivisi() {
        String nama = LoginCli.bacaString("Nama divisi   : ");
        String deskripsi = LoginCli.bacaString("Deskripsi     : ");
        int kuota = LoginCli.bacaInt("Kuota anggota : ");
        Divisi baru = storage.tambahDivisi(admin.getUsername(), nama, deskripsi, kuota);
        LoginCli.simpan(storage);
        LoginCli.sukses("Divisi " + baru.getNama() + " ditambahkan dengan kode " + baru.getId() + ".");
    }

    /** Mengubah data divisi; input kosong berarti nilai lama dipertahankan. */
    private void editDivisi() {
        LoginCli.tabelDivisi(storage, storage.getDivisiList());
        Divisi lama = storage.cariDivisi(LoginCli.bacaString("Kode divisi yang diedit: "));
        LoginCli.info("Kosongkan (Enter) jika tidak ingin mengubah.");
        String nama = LoginCli.bacaString("Nama baru [" + lama.getNama() + "]: ");
        String deskripsi = LoginCli.bacaString("Deskripsi baru [" + LoginCli.potong(lama.getDeskripsi(), 25) + "]: ");
        String kuotaTeks = LoginCli.bacaString("Kuota baru [" + lama.getKuota() + "]: ");
        int kuota = parseAngkaAtauLama(kuotaTeks, lama.getKuota());
        storage.editDivisi(admin.getUsername(), lama.getId(), nama.isEmpty() ? lama.getNama() : nama,
                deskripsi.isEmpty() ? lama.getDeskripsi() : deskripsi, kuota);
        LoginCli.simpan(storage);
        LoginCli.sukses("Divisi berhasil diperbarui.");
    }

    /** Menghapus divisi setelah konfirmasi. */
    private void hapusDivisi() {
        LoginCli.tabelDivisi(storage, storage.getDivisiList());
        Divisi divisi = storage.cariDivisi(LoginCli.bacaString("Kode divisi yang dihapus: "));
        if (LoginCli.konfirmasi("Hapus divisi " + divisi.getNama() + " beserta anggota dan tugasnya?")) {
            storage.hapusDivisi(admin.getUsername(), divisi.getId());
            LoginCli.simpan(storage);
            LoginCli.sukses("Divisi berhasil dihapus.");
        } else {
            LoginCli.info("Penghapusan dibatalkan.");
        }
    }

    // --------------------------------------------------------------- ANGGOTA

    /** Menampilkan submenu kelola anggota (lihat, tambah, edit, hapus). */
    private void menuAnggota() {
        while (true) {
            LoginCli.judul("KELOLA ANGGOTA");
            LoginCli.menu("1", "Lihat semua anggota (per divisi)");
            LoginCli.menu("2", "Lihat anggota satu divisi");
            LoginCli.menu("3", "Tambah anggota");
            LoginCli.menu("4", "Edit anggota");
            LoginCli.menu("5", "Hapus anggota");
            LoginCli.menu("0", "Kembali");
            int pilihan = LoginCli.bacaInt("Pilih menu: ");
            try {
                switch (pilihan) {
                    case 1:
                        tampilkanAnggotaPerDivisi(storage.getDivisiList());
                        break;
                    case 2:
                        LoginCli.tabelDivisi(storage, storage.getDivisiList());
                        Divisi d = storage.cariDivisi(LoginCli.bacaString("Kode divisi: "));
                        List<Divisi> satu = new ArrayList<>();
                        satu.add(d);
                        tampilkanAnggotaPerDivisi(satu);
                        break;
                    case 3:
                        tambahAnggota();
                        break;
                    case 4:
                        editAnggota();
                        break;
                    case 5:
                        hapusAnggota();
                        break;
                    case 0:
                        return;
                    default:
                        LoginCli.error("Pilihan tidak tersedia.");
                }
            } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
                LoginCli.error(e.getMessage());
            }
        }
    }

    /**
     * Mencetak anggota aktif, dipisah menjadi satu blok untuk setiap divisi.
     *
     * @param divisiList divisi yang ditampilkan anggotanya
     */
    private void tampilkanAnggotaPerDivisi(List<Divisi> divisiList) {
        if (divisiList.isEmpty()) {
            LoginCli.info("Belum ada divisi.");
            return;
        }
        for (Divisi d : divisiList) {
            List<Anggota> anggota = storage.getAnggotaByDivisi(d.getId());
            System.out.println();
            LoginCli.garis();
            System.out.println("DIVISI " + d.getNama().toUpperCase() + " (" + d.getId() + ")  -  "
                    + anggota.size() + "/" + d.getKuota() + " anggota");
            LoginCli.garis();
            if (anggota.isEmpty()) {
                System.out.println("  (belum ada anggota)");
                continue;
            }
            System.out.printf("%-5s %-26s %s%n", "ID", "Nama", "Jabatan");
            for (Anggota a : anggota) {
                System.out.printf("%-5s %-26s %s%n", a.getId(), LoginCli.potong(a.getNama(), 26), a.getJabatan());
            }
        }
        LoginCli.garis();
    }

    /** Meminta data anggota baru lalu menyimpannya (langsung berstatus aktif). */
    private void tambahAnggota() {
        LoginCli.tabelDivisi(storage, storage.getDivisiList());
        String idDivisi = LoginCli.bacaString("Kode divisi tujuan: ");
        String nama = LoginCli.bacaString("Nama anggota      : ");
        String jabatan = LoginCli.bacaString("Jabatan           : ");
        Anggota baru = storage.tambahAnggota(admin.getUsername(), nama, jabatan, idDivisi);
        LoginCli.simpan(storage);
        LoginCli.sukses("Anggota " + baru.getNama() + " ditambahkan dengan kode " + baru.getId() + ".");
    }

    /** Mengubah data anggota; input kosong berarti nilai lama dipertahankan. */
    private void editAnggota() {
        tampilkanAnggotaPerDivisi(storage.getDivisiList());
        Anggota lama = storage.cariAnggota(LoginCli.bacaString("Kode anggota yang diedit: "));
        LoginCli.info("Kosongkan (Enter) jika tidak ingin mengubah.");
        String nama = LoginCli.bacaString("Nama baru [" + lama.getNama() + "]: ");
        String jabatan = LoginCli.bacaString("Jabatan baru [" + lama.getJabatan() + "]: ");
        String divisi = LoginCli.bacaString("Kode divisi baru [" + lama.getIdDivisi() + "]: ");
        storage.editAnggota(admin.getUsername(), lama.getId(), nama.isEmpty() ? lama.getNama() : nama,
                jabatan.isEmpty() ? lama.getJabatan() : jabatan, divisi.isEmpty() ? lama.getIdDivisi() : divisi);
        LoginCli.simpan(storage);
        LoginCli.sukses("Anggota berhasil diperbarui.");
    }

    /** Menghapus anggota setelah konfirmasi. */
    private void hapusAnggota() {
        tampilkanAnggotaPerDivisi(storage.getDivisiList());
        Anggota anggota = storage.cariAnggota(LoginCli.bacaString("Kode anggota yang dihapus: "));
        if (LoginCli.konfirmasi("Hapus anggota " + anggota.getNama() + "?")) {
            storage.hapusAnggota(admin.getUsername(), anggota.getId());
            LoginCli.simpan(storage);
            LoginCli.sukses("Anggota berhasil dihapus.");
        } else {
            LoginCli.info("Penghapusan dibatalkan.");
        }
    }

    // ------------------------------------------- VERIFIKASI PENDAFTARAN PANITIA

    /** Menampilkan pendaftaran panitia yang menunggu, lalu admin menerima atau menolaknya. */
    private void verifikasiPendaftaran() {
        LoginCli.judul("VERIFIKASI PENDAFTARAN PANITIA");
        List<Anggota> menunggu = storage.getPendaftaranMenunggu();
        if (menunggu.isEmpty()) {
            LoginCli.info("Tidak ada pendaftaran yang menunggu verifikasi.");
            LoginCli.tekanEnter();
            return;
        }
        LoginCli.garis();
        System.out.printf("%-5s %-22s %-14s %-16s %s%n", "ID", "Nama", "Username", "Divisi tujuan", "Sisa kuota");
        LoginCli.garis();
        for (Anggota a : menunggu) {
            Divisi d = storage.cariDivisi(a.getIdDivisi());
            System.out.printf("%-5s %-22s %-14s %-16s %d%n", a.getId(), LoginCli.potong(a.getNama(), 22),
                    LoginCli.potong(a.getUsername(), 14), LoginCli.potong(d.getNama(), 16),
                    d.getKuota() - storage.jumlahAnggota(d.getId()));
        }
        LoginCli.garis();
        String id = LoginCli.bacaString("Kode pendaftaran (kosong = batal): ");
        if (id.isEmpty()) {
            return;
        }
        LoginCli.menu("1", "Terima (jadi anggota divisi)");
        LoginCli.menu("2", "Tolak");
        int aksi = LoginCli.bacaInt("Pilih: ");
        if (aksi != 1 && aksi != 2) {
            LoginCli.error("Pilihan tidak tersedia.");
            return;
        }
        storage.verifikasiPendaftaran(admin.getUsername(), id, aksi == 1);
        LoginCli.simpan(storage);
        LoginCli.sukses(aksi == 1 ? "Pendaftaran diterima. Member kini resmi menjadi anggota divisi."
                : "Pendaftaran ditolak.");
    }

    // ----------------------------------------------------------------- TUGAS

    /** Menampilkan submenu kelola tugas (lihat, tambah, edit, hapus). */
    private void menuTugas() {
        while (true) {
            LoginCli.judul("KELOLA TUGAS");
            LoginCli.menu("1", "Lihat semua tugas");
            LoginCli.menu("2", "Tambah tugas");
            LoginCli.menu("3", "Edit tugas");
            LoginCli.menu("4", "Hapus tugas");
            LoginCli.menu("0", "Kembali");
            int pilihan = LoginCli.bacaInt("Pilih menu: ");
            try {
                switch (pilihan) {
                    case 1:
                        LoginCli.tabelTugas(storage, storage.getTugasList());
                        break;
                    case 2:
                        tambahTugas();
                        break;
                    case 3:
                        editTugas();
                        break;
                    case 4:
                        hapusTugas();
                        break;
                    case 0:
                        return;
                    default:
                        LoginCli.error("Pilihan tidak tersedia.");
                }
            } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
                LoginCli.error(e.getMessage());
            }
        }
    }

    /** Meminta data tugas baru lalu menyimpannya. */
    private void tambahTugas() {
        LoginCli.tabelDivisi(storage, storage.getDivisiList());
        String idDivisi = LoginCli.bacaString("Kode divisi pemilik tugas: ");
        String judul = LoginCli.bacaString("Judul tugas              : ");
        String deskripsi = LoginCli.bacaString("Deskripsi                : ");
        String deadline = LoginCli.bacaString("Deadline (yyyy-MM-dd)    : ");
        Tugas baru = storage.tambahTugas(admin.getUsername(), judul, deskripsi, idDivisi, deadline);
        LoginCli.simpan(storage);
        LoginCli.sukses("Tugas " + baru.getJudul() + " ditambahkan dengan kode " + baru.getId() + ".");
    }

    /** Mengubah data tugas; input kosong berarti nilai lama dipertahankan. */
    private void editTugas() {
        LoginCli.tabelTugas(storage, storage.getTugasList());
        Tugas lama = storage.cariTugas(LoginCli.bacaString("Kode tugas yang diedit: "));
        LoginCli.info("Kosongkan (Enter) jika tidak ingin mengubah.");
        String judul = LoginCli.bacaString("Judul baru [" + lama.getJudul() + "]: ");
        String deskripsi = LoginCli.bacaString("Deskripsi baru [" + LoginCli.potong(lama.getDeskripsi(), 25) + "]: ");
        String deadline = LoginCli.bacaString("Deadline baru [" + lama.getDeadline() + "]: ");
        storage.editTugas(admin.getUsername(), lama.getId(), judul.isEmpty() ? lama.getJudul() : judul,
                deskripsi.isEmpty() ? lama.getDeskripsi() : deskripsi,
                deadline.isEmpty() ? lama.getDeadline().toString() : deadline);
        LoginCli.simpan(storage);
        LoginCli.sukses("Tugas berhasil diperbarui.");
    }

    /** Menghapus tugas setelah konfirmasi. */
    private void hapusTugas() {
        LoginCli.tabelTugas(storage, storage.getTugasList());
        Tugas tugas = storage.cariTugas(LoginCli.bacaString("Kode tugas yang dihapus: "));
        if (LoginCli.konfirmasi("Hapus tugas " + tugas.getJudul() + "?")) {
            storage.hapusTugas(admin.getUsername(), tugas.getId());
            LoginCli.simpan(storage);
            LoginCli.sukses("Tugas berhasil dihapus.");
        } else {
            LoginCli.info("Penghapusan dibatalkan.");
        }
    }

    /** Menampilkan tugas yang diajukan selesai, lalu admin menerima atau menolaknya. */
    private void verifikasiTugas() {
        LoginCli.judul("VERIFIKASI TUGAS");
        List<Tugas> menunggu = storage.getTugasMenunggu();
        LoginCli.tabelTugas(storage, menunggu);
        if (menunggu.isEmpty()) {
            LoginCli.tekanEnter();
            return;
        }
        String id = LoginCli.bacaString("Kode tugas (kosong = batal): ");
        if (id.isEmpty()) {
            return;
        }
        LoginCli.menu("1", "Terima");
        LoginCli.menu("2", "Tolak (kerjakan ulang)");
        int aksi = LoginCli.bacaInt("Pilih: ");
        if (aksi != 1 && aksi != 2) {
            LoginCli.error("Pilihan tidak tersedia.");
            return;
        }
        storage.verifikasiTugas(admin.getUsername(), id, aksi == 1);
        LoginCli.simpan(storage);
        LoginCli.sukses(aksi == 1 ? "Tugas diterima dan ditandai SELESAI." : "Tugas ditolak, dikembalikan ke pengerjanya.");
    }

    // ------------------------------------------------- MEMBER, RIWAYAT, LAPORAN

    /** Menampilkan daftar seluruh akun member beserta divisinya (bila sudah resmi jadi panitia). */
    private void daftarMember() {
        LoginCli.judul("DAFTAR MEMBER");
        List<Member> members = storage.getSemuaMember();
        if (members.isEmpty()) {
            LoginCli.info("Belum ada member terdaftar.");
        } else {
            LoginCli.garis();
            System.out.printf("%-4s %-16s %-22s %-12s %-8s %s%n", "No", "Username", "Nama", "NIM", "Divisi", "Jurusan");
            LoginCli.garis();
            int no = 1;
            for (Member m : members) {
                Anggota a = storage.cariAnggotaAktifByUsername(m.getUsername());
                System.out.printf("%-4d %-16s %-22s %-12s %-8s %s%n", no++, LoginCli.potong(m.getUsername(), 16),
                        LoginCli.potong(m.getNamaLengkap(), 22), m.getNim(), a == null ? "-" : a.getIdDivisi(),
                        m.getJurusan());
            }
            LoginCli.garis();
        }
        LoginCli.tekanEnter();
    }

    /** Menampilkan riwayat aktivitas seluruh admin. */
    private void riwayatAdmin() {
        LoginCli.judul("RIWAYAT AKTIVITAS ADMIN");
        List<RiwayatAdmin> data = storage.getRiwayatAdmin();
        if (data.isEmpty()) {
            LoginCli.info("Belum ada aktivitas.");
        } else {
            LoginCli.garis();
            System.out.printf("%-20s %-10s %-20s %s%n", "Waktu", "Admin", "Aksi", "Detail");
            LoginCli.garis();
            for (RiwayatAdmin r : data) {
                System.out.printf("%-20s %-10s %-20s %s%n", r.getWaktu().format(RiwayatAdmin.FORMAT),
                        LoginCli.potong(r.getUsername(), 10), r.getAksi(), LoginCli.potong(r.getDetail(), 38));
            }
            LoginCli.garis();
        }
        LoginCli.tekanEnter();
    }

    /** Menampilkan riwayat transaksi seluruh member. */
    private void riwayatMember() {
        LoginCli.judul("RIWAYAT TRANSAKSI MEMBER");
        List<RiwayatMember> data = storage.getRiwayatMemberSemua();
        if (data.isEmpty()) {
            LoginCli.info("Belum ada transaksi member.");
        } else {
            LoginCli.garis();
            System.out.printf("%-20s %-12s %-19s %s%n", "Waktu", "Member", "Aksi", "Detail");
            LoginCli.garis();
            for (RiwayatMember r : data) {
                System.out.printf("%-20s %-12s %-19s %s%n", r.getWaktu().format(RiwayatAdmin.FORMAT),
                        LoginCli.potong(r.getUsername(), 12), r.getAksi(), LoginCli.potong(r.getDetail(), 38));
            }
            LoginCli.garis();
        }
        LoginCli.tekanEnter();
    }

    /** Membuat laporan rekap per divisi, menampilkannya, dan menyimpannya ke file laporan.txt. */
    private void laporanRekap() {
        LoginCli.judul("LAPORAN REKAP KEPANITIAAN");
        List<String> laporan = new ArrayList<>();
        laporan.add("LAPORAN REKAP KEPANITIAAN");
        laporan.add(String.format("%-5s %-18s %-6s %-8s %-7s %-9s", "ID", "Divisi", "Kuota", "Anggota", "Tugas", "Selesai"));
        for (Divisi d : storage.getDivisiList()) {
            List<Tugas> tugas = storage.getTugasByDivisi(d.getId());
            long selesai = tugas.stream().filter(t -> t.getStatus() == Tugas.Status.SELESAI).count();
            laporan.add(String.format("%-5s %-18s %-6d %-8d %-7d %-9d", d.getId(), LoginCli.potong(d.getNama(), 18),
                    d.getKuota(), storage.jumlahAnggota(d.getId()), tugas.size(), selesai));
        }
        laporan.add("Total member                  : " + storage.getSemuaMember().size());
        laporan.add("Total anggota aktif           : " + storage.getAnggotaList().size());
        laporan.add("Pendaftaran menunggu verifikasi: " + storage.getPendaftaranMenunggu().size());
        laporan.add("Total tugas                   : " + storage.getTugasList().size());
        for (String baris : laporan) {
            System.out.println(baris);
        }
        Path file = storage.simpanLaporan(laporan);
        if (file != null) {
            LoginCli.sukses("Laporan juga disimpan ke " + file);
        } else {
            LoginCli.error("Laporan gagal disimpan ke file.");
        }
        LoginCli.tekanEnter();
    }

    /**
     * Mengubah teks menjadi angka, atau memakai nilai lama bila teks kosong.
     *
     * @param teks input pengguna
     * @param lama nilai lama yang dipakai bila input kosong
     * @return angka hasil konversi
     * @throws IllegalArgumentException jika input bukan angka bulat
     */
    private int parseAngkaAtauLama(String teks, int lama) {
        if (teks.isEmpty()) {
            return lama;
        }
        try {
            return Integer.parseInt(teks);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Kuota harus berupa angka bulat.");
        }
    }
}
