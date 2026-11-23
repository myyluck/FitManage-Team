# 🏋️ FitManage — Sistem Manajemen Gym

**FitManage** adalah aplikasi desktop manajemen operasional pusat kebugaran (*gym*) berbasis **Java Swing** dengan antarmuka modern **Dark-Mode** dan penyimpanan data *cloud* terpusat menggunakan **PostgreSQL (Supabase)**. Aplikasi ini dibangun untuk mendigitalisasi pendataan member, presensi *check-in* harian, penjadwalan kelas, manajemen *personal trainer*, transaksi paket, hingga pelaporan analitik secara *real-time*.

---

## ✨ Fitur Utama

* **Dashboard Analitik Real-Time**
  * **Kartu KPI Otomatis:** Menampilkan ringkasan Total Member (Reguler vs VIP/Personal Trainer), Check-In Hari Ini, Trainer Aktif, dan Total Pendapatan Paket Lunas.
  * **Combo Chart Dinamis (`LineChartPanel`):** Visualisasi tren kunjungan dan pendaftaran member bulanan dengan skala sumbu otomatis (*auto-scaling*) yang tersinkronisasi langsung dengan database.
  * **Donut Chart Kehadiran (`PieChartPanel`):** Menampilkan proporsi kehadiran harian berdasarkan kategori keanggotaan (**Reguler** dan **VIP / Personal Trainer**).
* **Manajemen Data Member**
  * Operasi **CRUD** (`AddMemberDialog`, `EditMemberDialog`) dengan pembuatan ID otomatis berurutan (`MEM-001`, `MEM-002`, dst.) yang memvalidasi seluruh rekaman di database.
  * Implementasi mekanisme **Soft Delete** untuk menjaga integritas riwayat presensi dan transaksi.
  * Pencarian cepat (*live search*) berdasarkan nama atau ID serta filter berdasarkan tipe member.
* **Sistem Check-In & Riwayat Presensi**
  * Pencatatan kehadiran member harian secara instan (`CheckInDialog`) beserta pembaruan tabel aktivitas terbaru di dashboard.
* **Manajemen Kelas, Jadwal & Trainer**
  * Pengelolaan jadwal sesi latihan (`AddScheduleForm`), instruktur pengampu (`AddTrainerForm`), serta pemantauan status keaktifan *personal trainer*.
* **Manajemen Paket & Transaksi**
  * Pembuatan paket keanggotaan (`CreatePackageForm`) serta pencatatan transaksi pembayaran beserta status pelunasannya.
* **Multi-Format Export Laporan (`ReportExporter`)**
  * Ekspor data laporan presensi dan operasional ke berbagai format dokumen (**Excel**, **CSV**, dan **HTML/PDF**).

---

## 🧠 Penerapan Konsep OOP & Arsitektur

Proyek ini dirancang menggunakan pendekatan **Object-Oriented Programming (OOP)** dan pemisahan tanggung jawab (*separation of concerns*):

* **Inheritance & Polymorphism:** Kelas induk `Member` diturunkan menjadi `MemberRegular`, `MemberVIP`, dan `PersonalTrainer` untuk menangani perbedaan hak akses, fasilitas, serta klasifikasi statistik kehadiran.
* **Singleton & State Management (`DataStore`):** Mengelola *state* aplikasi di memori secara terpusat agar sinkron antar-panel tampilan dan komponen grafik.
* **Database Layer (`KoneksiDB`):** Memisahkan koneksi **JDBC PostgreSQL (Supabase)** dan eksekusi kueri dari lapisan antarmuka pengguna (*UI*).
* **Custom UI Components:** Kustomisasi komponen Java Swing (`ThemeUI`, `BrandHeader`, `RoundedPanel`, `AppLogo`, `FormDialog`, `FormPanel`) dipadukan dengan **FlatLaf** untuk menghasilkan tampilan *dark-mode* yang konsisten.

---

## 📁 Struktur Proyek

```text
FitManage-Team/
├── README.md
└── fitmanage/
    ├── src/
    │   └── fitmanage/
    │       ├── Fitmanage.java          # Entry point utama aplikasi
    │       ├── Login.java              # Halaman autentikasi admin
    │       ├── Dashboard.java          # Antarmuka utama & navigasi multi-modul
    │       ├── DataStore.java          # Pengelola state, agregasi data & akses DB
    │       ├── KoneksiDB.java          # Konfigurasi koneksi JDBC PostgreSQL (Supabase)
    │       ├── Member.java             # Kelas induk entitas member
    │       ├── MemberRegular.java      # Subclass member reguler
    │       ├── MemberVIP.java          # Subclass member VIP
    │       ├── PersonalTrainer.java    # Subclass member dengan personal trainer
    │       ├── Presensi.java           # Model data presensi check-in
    │       ├── Jadwal.java             # Model data kelas & jadwal
    │       ├── Transaksi.java          # Model data paket & pembayaran
    │       ├── AddMemberDialog.java    # Dialog tambah member baru
    │       ├── EditMemberDialog.java   # Dialog ubah data member
    │       ├── CheckInDialog.java      # Dialog pencatatan check-in harian
    │       ├── AddScheduleForm.java    # Form tambah jadwal kelas
    │       ├── AddTrainerForm.java     # Form tambah data trainer
    │       ├── CreatePackageForm.java  # Form pembuatan paket gym
    │       ├── FormDialog.java         # Komponen dasar dialog form
    │       ├── FormPanel.java          # Komponen dasar panel form
    │       ├── LineChartPanel.java     # Komponen combo chart tren bulanan
    │       ├── PieChartPanel.java      # Komponen donut chart komposisi kehadiran
    │       ├── ReportExporter.java     # Utilitas ekspor laporan (Excel, CSV, HTML/PDF)
    │       ├── ThemeUI.java            # Konfigurasi palet warna & tipografi dark-mode
    │       ├── BrandHeader.java        # Komponen header navigasi & identitas aplikasi
    │       ├── RoundedPanel.java       # Panel kustom dengan sudut membulat
    │       ├── AppLogo.java            # Renderer logo resolusi tinggi
    │       └── logo_fitManage.png      # Aset logo aplikasi
    └── nbproject/                      # Konfigurasi proyek Apache NetBeans
```

---

## 🛠️ Teknologi yang Digunakan

* **Bahasa Pemrograman:** Java (JDK)
* **GUI Framework:** Java Swing & FlatLaf Look and Feel
* **Database:** PostgreSQL (hosted on Supabase)
* **Konektivitas:** PostgreSQL JDBC Driver
* **IDE & Version Control:** Apache NetBeans & Git / GitHub

---

## 🚀 Cara Menjalankan Proyek

1. **Clone repository ini:**
   ```bash
   git clone [https://github.com/myyluck/FitManage-Team.git](https://github.com/myyluck/FitManage-Team.git)
   ```
2. **Buka proyek di Apache NetBeans:**
   * Pilih **File → Open Project...** dan arahkan ke folder `fitmanage`.
3. **Pastikan konfigurasi database & library:**
   * Pastikan file `KoneksiDB.java` tersedia di dalam package `fitmanage` dengan kredensial database Supabase yang valid.
   * Pastikan library **PostgreSQL JDBC Driver** dan **FlatLaf** sudah terpasang di bagian **Libraries** proyek.
4. **Jalankan aplikasi:**
   * Klik kanan proyek `fitmanage` → **Run** (atau tekan `F6`).

---

## 👨‍💻 Tim Pengembang

Dikembangkan untuk memenuhi tugas proyek mata kuliah **Pemrograman Berorientasi Objek (PBO)**,Program Studi **Sistem Informasi**, **Universitas Singaperbangsa Karawang (UNSIKA)**:

* **Mukhlash Hisyam** ([@myyluck](https://github.com/myyluck)) — *Backend Logic, Supabase Integration, Data Visualization & Git Merge*
* **Aqila Maulana Ikhsan** ([@amikhsan](https://github.com/amikhsan)) — *UI/UX Dark-Mode Design & Feature Development*
