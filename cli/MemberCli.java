package cli;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import model.Anggota;
import model.DataStorage;
import model.Divisi;
import model.Member;
import model.RiwayatAdmin;
import model.RiwayatMember;
import model.Tugas;

/**
 * Menu untuk member: profil, mendaftar jadi panitia (perlu verifikasi admin), mengambil dan menyelesaikan
 * tugas, serta melihat riwayat.
 */
public class MemberCli {

    private final DataStorage storage;
    private final Member member;

    /**
     * Membuat menu member.
     *
     * @param storage pusat data aplikasi
     * @param member  akun member yang sedang login
     */
    public MemberCli(DataStorage storage, Member member) {
        this.storage = storage;
        this.member = member;
    }

    /** Menjalankan perulangan menu member sampai logout. */
    public void jalankan() {
        while (true) {
            LoginCli.judul("MENU MEMBER - " + member.getNamaLengkap());
            LoginCli.menu("1", "Profil Saya");
            LoginCli.menu("2", "Lihat Daftar Divisi");
            LoginCli.menu("3", "Daftar Jadi Panitia (perlu verifikasi admin)");
            LoginCli.menu("4", "Lihat Tugas Divisi Saya");
            LoginCli.menu("5", "Ambil Tugas");
            LoginCli.menu("6", "Ajukan Tugas Selesai");
            LoginCli.menu("7", "Tugas Saya");
            LoginCli.menu("8", "Riwayat Saya");
            LoginCli.menu("0", "Logout");
            int pilihan = LoginCli.bacaInt("Pilih menu: ");
            try {
                switch (pilihan) {
                    case 1:
                        profil();
                        break;
                    case 2:
                        lihatDivisi();
                        break;
                    case 3:
                        daftarPanitia();
                        break;
                    case 4:
                        lihatTugasDivisi();
                        break;
                    case 5:
                        ambilTugas();
                        break;
                    case 6:
                        ajukanSelesai();
                        break;
                    case 7:
                        LoginCli.judul("TUGAS SAYA");
                        LoginCli.tabelTugas(storage, storage.getTugasByPengambil(member.getUsername()));
                        LoginCli.tekanEnter();
                        break;
                    case 8:
                        riwayatSaya();
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

    /** Menampilkan profil member, status kepanitiaannya, dan menawarkan perubahan nama/jurusan. */
    private void profil() {
        LoginCli.judul("PROFIL SAYA");
        System.out.println("Username : " + member.getUsername());
        System.out.println("Nama     : " + member.getNamaLengkap());
        System.out.println("NIM      : " + member.getNim());
        System.out.println("Jurusan  : " + member.getJurusan());
        System.out.println("Panitia  : " + statusKepanitiaan());
        if (LoginCli.konfirmasi("Ubah nama/jurusan?")) {
            String nama = LoginCli.bacaString("Nama baru (Enter = tetap): ");
            String jurusan = LoginCli.bacaString("Jurusan baru (Enter = tetap): ");
            if (!nama.isEmpty()) {
                member.setNamaLengkap(nama);
            }
            if (!jurusan.isEmpty()) {
                member.setJurusan(jurusan);
            }
            LoginCli.simpan(storage);
            LoginCli.sukses("Profil diperbarui.");
        }
    }

    /**
     * Menyusun teks status kepanitiaan member (belum mendaftar, menunggu, aktif, atau ditolak).
     *
     * @return teks status kepanitiaan
     */
    private String statusKepanitiaan() {
        Anggota anggota = storage.cariAnggotaByUsername(member.getUsername());
        if (anggota == null) {
            return "Belum mendaftar sebagai panitia";
        }
        String divisi = storage.cariDivisi(anggota.getIdDivisi()).getNama();
        switch (anggota.getStatus()) {
            case AKTIF:
                return "Anggota divisi " + divisi + " (" + anggota.getJabatan() + ")";
            case MENUNGGU:
                return "Menunggu verifikasi admin untuk divisi " + divisi;
            default:
                return "Pendaftaran ke divisi " + divisi + " ditolak (boleh mendaftar ulang)";
        }
    }

    /** Menampilkan daftar divisi dengan pilihan lihat semua, cari, atau urutkan berdasarkan sisa kuota. */
    private void lihatDivisi() {
        LoginCli.judul("DAFTAR DIVISI");
        LoginCli.menu("1", "Semua divisi");
        LoginCli.menu("2", "Cari berdasarkan nama");
        LoginCli.menu("3", "Urutkan berdasarkan sisa kuota");
        int pilihan = LoginCli.bacaInt("Pilih: ");
        switch (pilihan) {
            case 1:
                LoginCli.tabelDivisi(storage, storage.getDivisiList());
                break;
            case 2:
                LoginCli.tabelDivisi(storage, storage.cariDivisiByNama(LoginCli.bacaString("Kata kunci: ")));
                break;
            case 3:
                LoginCli.tabelDivisi(storage, storage.urutkanDivisiBySisaKuota());
                break;
            default:
                LoginCli.error("Pilihan tidak tersedia.");
        }
        LoginCli.tekanEnter();
    }

    /** Mengirim pendaftaran jadi panitia ke sebuah divisi; baru resmi setelah diverifikasi admin. */
    private void daftarPanitia() {
        LoginCli.judul("DAFTAR JADI PANITIA");
        LoginCli.info("Status saat ini: " + statusKepanitiaan());
        LoginCli.tabelDivisi(storage, storage.getDivisiList());
        String id = LoginCli.bacaString("Kode divisi tujuan (kosong = batal): ");
        if (id.isEmpty()) {
            return;
        }
        Anggota pendaftaran = storage.daftarPanitia(member.getUsername(), id);
        LoginCli.simpan(storage);
        LoginCli.sukses("Pendaftaran ke divisi " + storage.cariDivisi(pendaftaran.getIdDivisi()).getNama()
                + " terkirim. Menunggu verifikasi admin.");
    }

    /**
     * Mengambil data anggota aktif milik member, atau menolak dengan pesan yang sesuai statusnya.
     *
     * @return data anggota aktif member
     * @throws IllegalStateException jika member belum mendaftar, masih menunggu, atau ditolak
     */
    private Anggota anggotaSaya() {
        Anggota anggota = storage.cariAnggotaByUsername(member.getUsername());
        if (anggota == null) {
            throw new IllegalStateException("Anda belum mendaftar sebagai panitia. Pilih menu 3 terlebih dahulu.");
        }
        if (anggota.getStatus() == Anggota.Status.MENUNGGU) {
            throw new IllegalStateException("Pendaftaran Anda masih menunggu verifikasi admin.");
        }
        if (anggota.getStatus() == Anggota.Status.DITOLAK) {
            throw new IllegalStateException("Pendaftaran Anda ditolak. Silakan daftar ulang lewat menu 3.");
        }
        return anggota;
    }

    /** Menampilkan seluruh tugas pada divisi tempat member menjadi panitia. */
    private void lihatTugasDivisi() {
        Anggota anggota = anggotaSaya();
        Divisi divisi = storage.cariDivisi(anggota.getIdDivisi());
        LoginCli.judul("TUGAS DIVISI " + divisi.getNama().toUpperCase());
        LoginCli.tabelTugas(storage, storage.getTugasByDivisi(divisi.getId()));
        LoginCli.tekanEnter();
    }

    /** Menampilkan tugas yang tersedia pada divisi member lalu mengambil salah satunya. */
    private void ambilTugas() {
        Anggota anggota = anggotaSaya();
        LoginCli.judul("AMBIL TUGAS");
        List<Tugas> tersedia = storage.getTugasByDivisi(anggota.getIdDivisi()).stream()
                .filter(t -> t.getStatus() == Tugas.Status.TERSEDIA)
                .collect(Collectors.toList());
        LoginCli.tabelTugas(storage, tersedia);
        if (tersedia.isEmpty()) {
            LoginCli.tekanEnter();
            return;
        }
        String id = LoginCli.bacaString("Kode tugas yang diambil (kosong = batal): ");
        if (id.isEmpty()) {
            return;
        }
        storage.ambilTugas(member.getUsername(), id);
        LoginCli.simpan(storage);
        LoginCli.sukses("Tugas berhasil diambil. Selamat mengerjakan!");
    }

    /** Menampilkan tugas yang sedang dikerjakan lalu mengajukan salah satunya sebagai selesai. */
    private void ajukanSelesai() {
        LoginCli.judul("AJUKAN TUGAS SELESAI");
        List<Tugas> dikerjakan = storage.getTugasByPengambil(member.getUsername()).stream()
                .filter(t -> t.getStatus() == Tugas.Status.DIAMBIL)
                .collect(Collectors.toList());
        LoginCli.tabelTugas(storage, dikerjakan);
        if (dikerjakan.isEmpty()) {
            LoginCli.tekanEnter();
            return;
        }
        String id = LoginCli.bacaString("Kode tugas yang selesai (kosong = batal): ");
        if (id.isEmpty()) {
            return;
        }
        storage.ajukanSelesai(member.getUsername(), id);
        LoginCli.simpan(storage);
        LoginCli.sukses("Tugas diajukan selesai. Menunggu verifikasi admin.");
    }

    /** Menampilkan riwayat transaksi milik member yang sedang login. */
    private void riwayatSaya() {
        LoginCli.judul("RIWAYAT SAYA");
        List<RiwayatMember> data = storage.getRiwayatMember(member.getUsername());
        if (data.isEmpty()) {
            LoginCli.info("Belum ada riwayat.");
        } else {
            LoginCli.garis();
            System.out.printf("%-20s %-19s %s%n", "Waktu", "Aksi", "Detail");
            LoginCli.garis();
            for (RiwayatMember r : data) {
                System.out.printf("%-20s %-19s %s%n", r.getWaktu().format(RiwayatAdmin.FORMAT), r.getAksi(),
                        LoginCli.potong(r.getDetail(), 36));
            }
            LoginCli.garis();
        }
        LoginCli.tekanEnter();
    }
}
