Saya ingin mengembangkan project NexPC yang sudah ada. Jangan membuat ulang project dari awal dan jangan menghapus fitur yang sudah berjalan.

Untuk tahap ini, tambahkan HANYA 2 fitur berikut:

1. FILE TRANSFER
Buat fitur untuk mengakses file antara HP Android dan PC Arch Linux melalui jaringan lokal.

Prioritas utama:
- Bisa memilih file dari HP.
- Bisa mengirim/share file dari HP ke PC.
- Bisa melihat progress upload.
- Menampilkan nama file, ukuran file, dan status transfer.
- Menampilkan riwayat transfer sederhana.
- Jika memungkinkan, tambahkan juga kemampuan melihat file/folder tertentu dari PC.
- Gunakan koneksi LAN/Wi-Fi lokal.
- Jangan menggunakan cloud atau server eksternal.
- Pastikan transfer aman dan hanya bisa dilakukan oleh device yang sudah terhubung ke NexPC.
- Jangan memberikan akses arbitrary filesystem tanpa batas; batasi akses ke direktori yang memang diperlukan.
- Jangan merusak fitur monitoring dan fitur NexPC yang sudah ada.

2. LAPTOP MODE
Tambahkan halaman khusus untuk informasi dan pengaturan laptop.

Tampilkan:
- Battery percentage
- Charging / Not Charging
- Battery status
- Power profile saat ini
- Jika tersedia, tampilkan battery health
- Jika tersedia, tampilkan estimasi waktu baterai
- CPU temperature
- CPU usage
- RAM usage

Tambahkan pilihan power profile jika didukung oleh sistem Arch Linux:
- Performance
- Balanced
- Power Saver

Perubahan power profile harus dilakukan melalui mekanisme Linux yang sesuai dan aman, bukan arbitrary shell command dari aplikasi.

UI/UX:
Saya ingin UI NexPC terlihat seperti aplikasi modern yang benar-benar dibuat oleh developer, BUKAN seperti dashboard AI generik.

Hindari:
- Terlalu banyak card
- Gradient berlebihan
- Glow/neon berlebihan
- Background hitam pekat di seluruh aplikasi
- Tampilan cyberpunk
- Icon AI yang tidak diperlukan
- Terlalu banyak informasi dalam satu layar
- Dashboard yang terlihat seperti template AI-generated

Gunakan prinsip:
- Clean
- Modern
- Minimal
- Professional
- Intuitive
- Banyak whitespace
- Typography yang jelas
- Hierarki informasi yang kuat
- Spacing yang konsisten
- Icon yang sederhana
- Warna accent secukupnya
- Komponen UI yang konsisten

Gunakan dark mode yang nyaman, bukan hitam pekat. Gunakan warna surface yang memiliki sedikit perbedaan tingkat gelap agar setiap section tetap jelas tanpa harus diberi card berlebihan.

Dashboard harus terasa seperti aplikasi system utility modern, bukan aplikasi AI.

UX:
- Navigasi harus sederhana.
- User harus bisa memahami fungsi setiap menu tanpa membaca dokumentasi.
- Jangan menampilkan semua informasi sekaligus.
- Gunakan progressive disclosure untuk informasi yang lebih detail.
- Loading, success, error, offline, dan transfer state harus memiliki feedback yang jelas.
- Jika PC offline, tampilkan state offline yang rapi dan jelas.
- Jangan menggunakan animasi berlebihan.

Struktur navigasi yang disarankan:

Home
├── PC Status
├── CPU / RAM / Disk
├── Temperature
└── Quick Actions

Files
├── Send to PC
├── PC Files
└── Transfer History

Laptop
├── Battery
├── Power Profile
└── Hardware Status

Processes
Settings

Jangan menambahkan fitur besar lainnya seperti Remote Screen, Package Manager, Remote Terminal, Automation, atau fitur AI pada tahap ini.

Prioritas utama:
1. Fitur berjalan dengan benar.
2. Komunikasi Android ↔ Arch stabil.
3. File transfer aman.
4. Laptop information akurat.
5. UI/UX terlihat profesional dan tidak generik.

Sebelum melakukan perubahan besar, pahami struktur project yang sudah ada dan gunakan arsitektur serta komponen yang sudah tersedia jika masih sesuai. Jangan mengganti teknologi utama project tanpa alasan yang kuat.