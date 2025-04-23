package com.projekan.tiket_pesawat.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
// import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.projekan.tiket_pesawat.dto.KursiResponseDto;
import com.projekan.tiket_pesawat.dto.PemesananTiketRequestDto;
import com.projekan.tiket_pesawat.dto.PenerbanganResponseDto;
import com.projekan.tiket_pesawat.dto.ResponseApi;
import com.projekan.tiket_pesawat.exception.TidakDitemukanException;
import com.projekan.tiket_pesawat.models.Booking;
import com.projekan.tiket_pesawat.models.KetersediaanPenerbangan;
import com.projekan.tiket_pesawat.models.Kursi;
import com.projekan.tiket_pesawat.models.Penerbangan;
import com.projekan.tiket_pesawat.models.Penumpang;
import com.projekan.tiket_pesawat.models.StatusPembayaran;
import com.projekan.tiket_pesawat.models.StatusPenerbangan;
import com.projekan.tiket_pesawat.models.Tiket;
import com.projekan.tiket_pesawat.models.User;
import com.projekan.tiket_pesawat.repository.BookingRepository;
import com.projekan.tiket_pesawat.repository.KursiRepository;
import com.projekan.tiket_pesawat.repository.PenerbanganRepository;
import com.projekan.tiket_pesawat.repository.PenumpangRepository;
import com.projekan.tiket_pesawat.repository.TiketRepository;
import com.projekan.tiket_pesawat.repository.UserRepository;
import com.projekan.tiket_pesawat.services.AdminService;
import com.projekan.tiket_pesawat.services.KursiService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

        private final PenerbanganRepository penerbanganRepository;
        private final TiketRepository tiketRepository;
        private final KursiRepository kursiRepository;
        private final PenumpangRepository penumpangRepository;
        private final BookingRepository bookingRepository;
        private final UserRepository userRepository;
        private final AdminService adminService;
        private final KursiService kursiService;

        @PostMapping(value = "/pemesanan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Pesan Tiket Pesawat", description = "Cek Dulu Penerbangan yang aktif")
        public ResponseEntity<?> buatPesananTiket(@ModelAttribute PemesananTiketRequestDto pemesanan,
                        Principal principal)
                        throws IOException {
                String email = principal.getName();
                User user = userRepository.findByEmail(email).orElseThrow();
                Penerbangan penerbangan = penerbanganRepository.findById(pemesanan.getPenerbanganId())
                                .orElseThrow(() -> new TidakDitemukanException("Data Penerbangan Tidak di Temukan"));

                if (penerbangan.getStatusPenerbangan().equals(StatusPenerbangan.DEPARTED)) {
                        return ResponseEntity.badRequest()
                                        .body(ResponseApi.gagal(
                                                        "Penerbangan dengan ID : " + penerbangan.getId()
                                                                        + "tidak dapat dilayani karena sudah berangkat",
                                                        null, HttpStatus.BAD_REQUEST.value()));
                } else if (penerbangan.getStatusPenerbangan().equals(StatusPenerbangan.ARRIVED)) {
                        return ResponseEntity.badRequest()
                                        .body(ResponseApi.gagal("Penerbangan dengan ID : " + penerbangan.getId()
                                                        + "tidak dapat dilayani karena penerbangan telah selesai", null,
                                                        HttpStatus.BAD_REQUEST.value()));
                }

                boolean semuaKursiSudahTerisi = kursiRepository.findByPenerbanganId(penerbangan.getId())
                                .stream()
                                .allMatch(kursi -> kursi.isKursiTersedia());

                if (semuaKursiSudahTerisi) {
                        penerbangan.setKetersediaanPenerbangan(KetersediaanPenerbangan.TIDAK_TERSEDIA);
                        penerbanganRepository.save(penerbangan);

                        return ResponseEntity.badRequest().body(ResponseApi.gagal(
                                        "Maaf Penerbangan Tersebut Tidak Tersedia, Mungkin tiket Sudah Habis", null,
                                        HttpStatus.BAD_REQUEST.value()));
                }

                String namaFile = UUID.randomUUID() + "_" + pemesanan.getFileKtp().getOriginalFilename();
                Path path = Paths.get("uploads/ktp/" + namaFile);
                Files.createDirectories(path.getParent());
                Files.write(path, pemesanan.getFileKtp().getBytes());

                BigDecimal hargaTiket = penerbangan.getHargaTiket();

                Penumpang penumpang = Penumpang.builder()
                                .nama(pemesanan.getNama())
                                .noHP(pemesanan.getNoHP())
                                .user(user)
                                .fileKtp(namaFile).build();

                String kodeBooking = "ASTRA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                Booking booking = Booking.builder()
                                .totalHarga(hargaTiket)
                                .kodeBooking(kodeBooking)
                                .penerbangan(penerbangan)
                                .penumpang(penumpang)
                                .statusPembayaran(StatusPembayaran.BELUM_DIBAYAR)
                                .waktuBooking(LocalDateTime.now())
                                .waktuPembayaran(null)
                                .batasWaktuPembayaran(LocalDateTime.now().plusMinutes(15)).build();

                penumpangRepository.save(penumpang);
                bookingRepository.save(booking);

                return ResponseEntity.ok(ResponseApi.sukses("Pesanan Berhasil Disimpan Silahkan Melakukan Pembayaran",
                                null, HttpStatus.OK.value()));
        }

        @GetMapping("/{idBooking}/verifikasi-pembayaran")
        public ResponseEntity<?> linkPembayaran(@PathVariable String kodeBooking, Principal principal) {
                Booking booking = bookingRepository.findByKodeBooking(kodeBooking).orElseThrow(
                                () -> new TidakDitemukanException(
                                                "Booking dengan Kode Booking : " + kodeBooking + " Tidak di Temukan"));

                String email = principal.getName();
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new TidakDitemukanException("user Tidak di Temukan"));

                if (booking.getPenumpang() == null || booking.getPenumpang().getUser() == null
                                || !booking.getPenumpang().getUser().equals(user)) {
                        return ResponseEntity.badRequest()
                                        .body(ResponseApi.gagal("Akses Di Tolak", null,
                                                        HttpStatus.BAD_REQUEST.value()));
                }

                String linkHtml = "http://localhost:8080/user/" + booking.getId() + "/halaman-verifikasi-pembayaran";

                Map<String, String> response = Map.of("pesan",
                                "Klik link di bawah ini di browser untuk verifikasi pembayaran:",
                                "url", linkHtml);

                return ResponseEntity.ok(response);
        }

        @PostMapping("/pemilihan-nomor-kursi")
        @Operation(summary = "Buat Pilih Nomor Kursi", description = "Silahkan Cek Dulu Kursi yang Tersedia, Sebelum Memilih Kursi Id")
        public ResponseEntity<?> pilihNomorKursi(@RequestParam String kodeBooking, @RequestParam Long kursiId,
                        Principal principal) {
                String email = principal.getName();
                User user = userRepository.findByEmail(email).orElseThrow();

                Booking booking = bookingRepository.findByKodeBooking(kodeBooking).orElseThrow(
                                () -> new TidakDitemukanException(
                                                "Booking dengan Kode Booking : " + kodeBooking + " Tidak di Temukan"));

                if (booking.getPenumpang() == null || booking.getPenumpang().getUser() == null
                                || !booking.getPenumpang().getUser().equals(user)) {
                        return ResponseEntity.badRequest()
                                        .body(ResponseApi.gagal("Info ada Kesalahan, Sesuaikan dengan Id User nya",
                                                        null,
                                                        HttpStatus.BAD_REQUEST.value()));
                }

                if (booking.getStatusPembayaran() != StatusPembayaran.SUDAH_DIBAYAR) {
                        return ResponseEntity.badRequest()
                                        .body(ResponseApi.gagal(
                                                        "Pembayaran Belum Selesai, Silahkan Selesaikan Terlebih Dahulu Pembayaran anda!",
                                                        null,
                                                        HttpStatus.BAD_REQUEST.value()));
                }

                if (tiketRepository.existsByBooking(booking)) {
                        return ResponseEntity.badRequest()
                                        .body(ResponseApi.gagal("Booking Ini Sudah Digunakan Untuk Tiket!", null,
                                                        HttpStatus.BAD_REQUEST.value()));
                }

                Kursi kursi = kursiRepository.findById(kursiId)
                                .orElseThrow(() -> new TidakDitemukanException("Data Kursi Tidak Di Temukan"));
                if (!kursi.isKursiTersedia()) {
                        return ResponseEntity.badRequest().body(ResponseApi.gagal("Nomor Kursi Sudah Digunakan", null,
                                        HttpStatus.BAD_REQUEST.value()));
                }

                kursi.setKursiTersedia(false);
                kursiRepository.save(kursi);

                Tiket tiket = Tiket.builder()
                                .user(user)
                                .penerbangan(booking.getPenerbangan())
                                .booking(booking)
                                .kursi(kursi).build();
                tiketRepository.save(tiket);

                return ResponseEntity.ok(ResponseApi.sukses(
                                "Tiket Berhasil Dibuat Dengan Nomor kursi + " + kursi.getNomorkursi(), null,
                                HttpStatus.OK.value()));
        }

        @GetMapping("/{tiketId}/download-tiket-PDF")
        public ResponseEntity<?> downloadTiket(@PathVariable Long tiketId, Principal principal) {
                try {
                        Tiket tiket = tiketRepository.findById(tiketId).orElseThrow(
                                        () -> new TidakDitemukanException("Data Tiket Anda Tidak Di Temukan"));

                        if (!tiket.getUser().getEmail().equals(principal.getName())) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                                .body(ResponseApi.gagal("Akses Di Tolak, Tiket Bukan Milik Anda", null,
                                                                HttpStatus.FORBIDDEN.value()));
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Document document = new Document(PageSize.A5.rotate(), 30, 30, 20, 20);
                        PdfWriter.getInstance(document, baos);
                        document.open();

                        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.BLUE);
                        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
                        Font valueFont = new Font(Font.FontFamily.HELVETICA, 12);

                        Paragraph title = new Paragraph("✈ BOARDING PASS ✈", titleFont);
                        title.setAlignment(Element.ALIGN_CENTER);
                        document.add(title);
                        document.add(new LineSeparator());
                        document.add(new Paragraph());

                        PdfPTable table = new PdfPTable(2);
                        table.setWidthPercentage(100);
                        table.setWidths(new int[] { 1, 2 });

                        table.addCell(new Phrase("Nama Penumpang: ", labelFont));
                        table.addCell(new Phrase(tiket.getBooking().getPenumpang().getNama(), valueFont));

                        table.addCell(new Phrase("Maskapai: ", labelFont));
                        table.addCell(new Phrase(tiket.getPenerbangan().getMaskapai(), valueFont));

                        table.addCell(new Phrase("Waktu Berangkat: ", labelFont));
                        table.addCell(new Phrase(tiket.getPenerbangan().getWaktuKeberangkatan().toString(), valueFont));

                        table.addCell(new Phrase("Waktu Kedatangan: ", labelFont));
                        table.addCell(new Phrase(tiket.getPenerbangan().getWaktuKedatangan().toString(), valueFont));

                        table.addCell(new Phrase("Nomor Kursi: ", labelFont));
                        table.addCell(new Phrase(tiket.getKursi().getNomorkursi(), valueFont));

                        table.addCell(new Phrase("Kode Booking: ", labelFont));
                        table.addCell(new Phrase(tiket.getBooking().getKodeBooking(), valueFont));

                        table.addCell(new Phrase("Status: ", labelFont));
                        table.addCell(new Phrase("SUDAH DIBAYAR", valueFont));

                        document.add(table);

                        document.add(new Paragraph(" "));
                        BarcodeQRCode qrCode = new BarcodeQRCode("Kode Booking: " + tiket.getBooking().getKodeBooking(),
                                        100, 100, null);
                        Image qrImage = qrCode.getImage();
                        qrImage.scaleAbsolute(100, 100);
                        qrImage.setAlignment(Image.ALIGN_RIGHT);
                        document.add(qrImage);

                        document.close();

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=tiket_" + tiket.getId())
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .body(baos.toByteArray());
                } catch (DocumentException error) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ResponseApi.gagal(
                                                        "Kesalahan saat memproses dokumen PDF: " + error.getMessage(),
                                                        null, HttpStatus.INTERNAL_SERVER_ERROR.value()));
                }
        }

        @GetMapping("/melihat-penerbangan-tersedia")
        public ResponseEntity<?> melihatPenerbangan(@RequestParam(required = false) String dari,
                        @RequestParam(required = false) String ke,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggal) {
                List<PenerbanganResponseDto> response = adminService.ambilPenerbanganTersedia(dari, ke, tanggal);
                return ResponseEntity.ok(
                                ResponseApi.sukses("Data Penerbangan Yang Tersedia", response, HttpStatus.OK.value()));
        }

        @GetMapping("/melihat-kursi-tersedia")
        public ResponseEntity<?> melihatDataKursi(@RequestParam String kodeBooking){
                List<KursiResponseDto> response = kursiService.ambilDataKursiTersedia(kodeBooking);
                return ResponseEntity.ok(ResponseApi.sukses("Data Berhasil Di Dapat",response, HttpStatus.OK.value()));
        }
}
