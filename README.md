# Tabungan Siswa — Android APK Wrapper

Aplikasi Android ini membuka web app Google Apps Script **Tabungan Siswa** di dalam
WebView native tanpa address bar browser.

URL web:
https://script.google.com/macros/s/AKfycbyDl65vCY1XbAOcIln5eUvkZGhLlP_oZad0l9RKteRux6Nrs5zEXbAhI__ftGSVvT_m/exec

## Fitur wrapper Android

- Launcher icon khusus.
- Splash screen.
- Tampilan fullscreen seperti aplikasi.
- JavaScript, DOM Storage, cookies, dan `google.script.run` tetap berjalan.
- Tombol Back Android mengikuti riwayat halaman.
- Dukungan popup.
- Dukungan `window.print()` memakai dialog Print Android.
- Download CSV/PDF berbasis Blob diarahkan ke folder **Download/Tabungan Siswa**.
- Download HTTP/HTTPS memakai Download Manager Android.
- Tidak menyimpan password atau data siswa di dalam APK.

## Cara membuat APK otomatis lewat GitHub

1. Buat repository GitHub baru.
2. Upload seluruh isi folder project ini ke repository.
3. Buka tab **Actions**.
4. Pilih **Build APK Tabungan Siswa**.
5. Klik **Run workflow**.
6. Setelah selesai, buka job yang berhasil.
7. Download artifact **Tabungan-Siswa-APK**.
8. Ekstrak ZIP artifact, lalu install `app-debug.apk` di HP Android.

APK debug ini sudah ditandatangani otomatis oleh Android build system dan dapat
diinstal untuk penggunaan internal/testing.
