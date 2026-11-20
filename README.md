# Sistem Manajemen Kepanitiaan (UAS PBO C - Kelompok 6)

Aplikasi berbasis **Java (CLI)** untuk mengelola kepanitiaan sebuah acara.
Member mendaftar menjadi panitia pada sebuah divisi dan **harus diverifikasi admin** terlebih dahulu.
Setelah resmi menjadi anggota, member mengambil dan menyelesaikan tugas, lalu admin memverifikasi hasilnya.

## Anggota & Pembagian Tugas

| Anggota | Tugas | File |
|---------|-------|------|
| Hanip   | Hierarki akun (abstract class, inheritance, polymorphism) | `model/Account.java`, `model/Admin.java`, `model/Member.java` |
| Faras   | Entitas data kepanitiaan dan riwayat | `model/Divisi.java`, `model/Anggota.java`, `model/Tugas.java`, `model/RiwayatAdmin.java` `model/RiwayatMember.java` |
| Imam    | Pusat data, logika bisnis, penyimpanan file | `model/DataStorage.java` |
| Syella  | Antarmuka CLI admin | `cli/AdminCli.java` |
| Tara    | Antarmuka login, CLI member, Main, README | `cli/LoginCli.java`, `cli/MemberCli.java`, `app/Main.java`, `README.md` |

> Sesuaikan tabel ini dengan kontribusi sebenarnya. Pastikan setiap anggota punya commit di repository.

## Fitur

**Admin**
1. Kelola **divisi**: lihat, tambah, edit, hapus, urutkan (sisa kuota terbanyak)
2. Kelola **anggota**: lihat semua (dipisah per divisi), lihat satu divisi, tambah, edit, hapus
3. Kelola **tugas**: lihat, tambah, edit, hapus
4. **Verifikasi pendaftaran panitia** (terima / tolak)
5. **Verifikasi tugas** yang diajukan member (terima / tolak untuk dikerjakan ulang)
6. Daftar member, riwayat aktivitas admin, riwayat transaksi member
7. Laporan rekap (tampil di layar dan tersimpan ke `data/laporan.txt`)

**Member**
1. Profil saya (termasuk status kepanitiaan)
2. Lihat daftar divisi (semua / cari / urutkan sisa kuota)
3. Daftar jadi panitia (perlu verifikasi admin)
4. Lihat tugas divisi saya
5. Ambil tugas
6. Ajukan tugas selesai
7. Tugas saya
8. Riwayat saya

**Aturan sistem**
- Member mendaftar ke divisi -> status `MENUNGGU`. Baru resmi menjadi anggota (`AKTIF`) setelah diterima admin.
- Jika ditolak (`DITOLAK`), member boleh mendaftar ulang ke divisi lain/yang sama.
- Kuota divisi hanya dihitung dari anggota `AKTIF`. Pendaftaran ditolak jika kuota penuh, dan penerimaan oleh admin
  dicek ulang terhadap kuota.
- Hanya anggota `AKTIF` yang dapat melihat/mengambil tugas, dan hanya tugas milik divisinya.
- Alur status tugas: `TERSEDIA -> DIAMBIL -> MENUNGGU_VERIFIKASI -> SELESAI` (jika ditolak kembali ke `DIAMBIL`).
- Data tersimpan otomatis ke folder `data/`.

## Akun Bawaan

| Role  | Username | Password |
|-------|----------|----------|
| Admin | `admin`  | `admin123` |

Akun member dibuat lewat menu **Registrasi Akun Member**.

## Cara Menjalankan

Prasyarat: JDK 11 atau lebih baru. Jalankan dari folder root repository (folder yang berisi `app`, `cli`, `model`).

```bash
# kompilasi
javac -d out -sourcepath . app/Main.java

# jalankan
java -cp out app.Main
```

Bila warna ANSI tidak tampil rapi di terminal, jalankan dengan variabel lingkungan `NO_COLOR=1`.

Membuat dokumentasi Javadoc:

```bash
javadoc -private -d docs -sourcepath . app cli model
```

## Struktur Folder

```
SISTEM MANAJEMEN KEPANITIAAN
├── app
│   └── Main.java            # titik masuk program
├── cli
│   ├── AdminCli.java        # menu admin
│   ├── LoginCli.java        # menu login/registrasi + utilitas tampilan & input
│   └── MemberCli.java       # menu member
├── model
│   ├── Account.java         # abstract class akun
│   ├── Admin.java           # extends Account
│   ├── Member.java          # extends Account
│   ├── Anggota.java         # anggota panitia + status verifikasi (enum Status)
│   ├── DataStorage.java     # koleksi data, logika bisnis, baca/tulis file
│   ├── Divisi.java
│   ├── RiwayatAdmin.java    # log aktivitas admin
│   ├── RiwayatMember.java   # log transaksi member
│   └── Tugas.java           # tugas + enum Status
└── README.md
```

## Alur Kerja Sistem

1. Member registrasi lalu login; admin login dengan akun bawaan.
2. Admin membuat divisi dan tugas.
3. Member mendaftar jadi panitia pada sebuah divisi (status `MENUNGGU`).
4. Admin memverifikasi pendaftaran. Jika diterima, member resmi menjadi anggota divisi.
5. Member mengambil tugas divisinya, lalu mengajukan penyelesaian.
6. Admin memverifikasi tugas. Seluruh aksi tercatat di riwayat admin dan riwayat member.

## Pemenuhan Syarat UAS

| Syarat | Letak kode |
|--------|-----------|
| Login 2 tipe akun (Admin, User) | `DataStorage.login`, `DataStorage.registrasiMember`, `LoginCli.login` |
| Data login disimpan sederhana | `DataStorage.accounts` (`LinkedHashMap`) dan file `data/akun.txt` |
| Admin: tambah/edit/hapus data utama | Divisi, anggota, tugas: `DataStorage.tambah*/edit*/hapus*`, menu di `AdminCli` |
| Admin: melihat data pengguna/transaksi | `AdminCli.daftarMember`, `riwayatAdmin`, `riwayatMember`, `verifikasiPendaftaran`, `verifikasiTugas` |
| User: melihat daftar data | `MemberCli.lihatDivisi`, `lihatTugasDivisi` |
| User: transaksi | `DataStorage.daftarPanitia`, `ambilTugas`, `ajukanSelesai` |
| User: riwayat transaksi | `DataStorage.getRiwayatMember`, `MemberCli.riwayatSaya` |
| Encapsulation | Semua atribut `private` + getter/setter di seluruh class `model` |
| Inheritance | `Admin` dan `Member` mewarisi `Account` |
| Polymorphism | Override `getRole()`, `getInfo()`, `toRecord()`; pemanggilan `akun.getInfo()` di `LoginCli.login` |
| Abstraction | `Account` (abstract class; method abstract `getRole`, `getInfo`, `toRecord`) |
| Collection | `ArrayList` (divisi, anggota, tugas, riwayat) dan `LinkedHashMap` (akun) di `DataStorage` |
| Exception handling | try-catch di semua menu CLI, `LoginCli.bacaInt`, dan operasi file di `DataStorage` |
| Javadoc | Semua class, konstruktor, dan method (termasuk private) |
| Fitur tambahan | Penyimpanan file, urutkan divisi, laporan rekap, riwayat admin, tampilan CLI berwarna |
