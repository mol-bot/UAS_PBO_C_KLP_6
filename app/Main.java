package app;

import cli.LoginCli;
import model.DataStorage;

/** Titik masuk program Sistem Manajemen Kepanitiaan (CLI). */
public class Main {

    /** Class ini hanya berisi method main, sehingga tidak perlu dibuat objeknya. */
    private Main() {
    }

    /**
     * Menjalankan program: memuat data dari file lalu membuka menu login.
     *
     * @param args argumen baris perintah (tidak dipakai)
     */
    public static void main(String[] args) {
        DataStorage storage = new DataStorage("data");
        if (!storage.muat()) {
            LoginCli.error("Sebagian data gagal dimuat. Program tetap berjalan dengan data yang tersedia.");
        }
        storage.simpan();
        new LoginCli(storage).jalankan();
    }
}