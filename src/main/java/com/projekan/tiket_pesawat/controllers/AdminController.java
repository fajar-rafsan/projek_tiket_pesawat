package com.projekan.tiket_pesawat.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projekan.tiket_pesawat.dto.PenerbanganDto;
import com.projekan.tiket_pesawat.dto.PenerbanganUpdateDto;
import com.projekan.tiket_pesawat.dto.ResponseApi;
import com.projekan.tiket_pesawat.exception.AdminException;
import com.projekan.tiket_pesawat.exception.TidakDitemukanException;
import com.projekan.tiket_pesawat.models.Penerbangan;
import com.projekan.tiket_pesawat.repository.PenerbanganRepository;
import com.projekan.tiket_pesawat.services.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
        private final PenerbanganRepository penerbanganRepository;
        private final AdminService adminService;

        @PostMapping("tambah-penerbangan")
        public ResponseEntity<ResponseApi<?>> tambahPenerbangan(@RequestBody @Valid PenerbanganDto request) {

                if (request.getKotaKeberangkatan().equalsIgnoreCase(request.getKotaTujuan())) {
                        String errorNya = "Kota keberangkatan dan tujuan tidak boleh di tempat yang sama";
                        return ResponseEntity.badRequest()
                                        .body(ResponseApi.gagal("info ada kesalahan", errorNya,
                                                        HttpStatus.BAD_REQUEST.value()));
                }

                if (!request.getWaktuKedatangan().isAfter(request.getWaktuKeberangkatan())) {
                        String errorNya = "Waktu tiba harus setelah waktu keberangkatan";
                        return ResponseEntity.badRequest()
                                        .body(ResponseApi.gagal("info ada kesalahan", errorNya,
                                                        HttpStatus.BAD_REQUEST.value()));

                }

                Penerbangan penerbanganDataBaru = Penerbangan.builder()
                                .maskapai(request.getMaskapai())
                                .kotaKeberangkatan(request.getKotaKeberangkatan())
                                .kotaTujuan(request.getKotaTujuan())
                                .waktuKeberangkatan(request.getWaktuKeberangkatan())
                                .waktuKedatangan(request.getWaktuKedatangan())
                                .hargaTiket(request.getHargaTiket())
                                .kursi(request.getKursi())
                                .histories(null).build();
                penerbanganRepository.save(penerbanganDataBaru);
                return ResponseEntity
                                .ok(ResponseApi.sukses("Data Penerbangan Berhasil Di Tambahkan", penerbanganDataBaru,
                                                HttpStatus.OK.value()));
        }

        @PutMapping("/update-data-penerbangan/{id}")
        public ResponseEntity<ResponseApi<?>> updateDataPenerbangan(@PathVariable Long id,
                        @RequestBody PenerbanganUpdateDto request,
                        @AuthenticationPrincipal UserDetails userNya) {
                String user = userNya.getUsername();
                Penerbangan penerbangan = penerbanganRepository.findById(id)
                                .orElseThrow(() -> new TidakDitemukanException(
                                                "Penerbangan dengan ID " + id + " Tidak ditemukan"));
                Map<String, Object> perubahan = adminService.updatePenerbangan(penerbangan, request, user);

                return ResponseEntity.ok(ResponseApi.sukses(
                                "Data Penerbangan Berhasil Di Update! dan di simpan di History Update", perubahan,
                                HttpStatus.OK.value()));
        }

        @GetMapping("/{penerbanganId}/ekspor-history-update-ke-excell")
        public ResponseEntity<?> exportUpdateKeExcell(@PathVariable Long penerbanganId) {
                try {
                        Penerbangan penerbangan = penerbanganRepository.findById(penerbanganId)
                                        .orElseThrow(() -> new TidakDitemukanException(
                                                        "Penerbangan dengan ID " + penerbanganId + " Tidak Ditemukan"));
                        ByteArrayInputStream fileExcell = adminService.eksportKeExcell(penerbangan);

                        HttpHeaders headers = new HttpHeaders();
                        headers.add("Content-Disposition",
                                        "attachment: filename=Data_Update_History ID " + penerbanganId + ".xlsx");

                        return ResponseEntity
                                        .ok()
                                        .headers(headers)
                                        .contentType(MediaType.parseMediaType(
                                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                                        .body(new InputStreamResource(fileExcell));
                } catch (IOException error) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ResponseApi.gagal("Info ada kesalahan", error.getMessage(),
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
                } catch (TidakDitemukanException error) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                        ResponseApi.gagal(error.getMessage(), null, HttpStatus.NOT_FOUND.value()));
                } catch (IllegalStateException error) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                        ResponseApi.gagal(error.getMessage(), null, HttpStatus.NOT_FOUND.value()));
                } catch (AdminException error) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ResponseApi.gagal(error.getMessage(), null,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
                }
        }

        @DeleteMapping("/{penerbanganId}/hapus-penerbangan")
        public ResponseEntity<ResponseApi<?>> hapusDataPenerbangan(@PathVariable Long penerbanganId) {
                Penerbangan penerbangan = penerbanganRepository.findById(penerbanganId)
                                .orElseThrow(() -> new TidakDitemukanException("Data Penerbangan Tidak Di Temukan!"));
                Penerbangan responsePenerbangan = penerbangan;
                penerbanganRepository.delete(penerbangan);

                return ResponseEntity
                                .ok(ResponseApi.sukses("Data Berhasil Di hapus", responsePenerbangan,
                                                HttpStatus.OK.value()));
        }

        @GetMapping("/Mengambil-semua-data-penerbangan")
        public ResponseEntity<ResponseApi<?>> tampilkanDataPenerbangan(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "waktuKeberangkatan") String urutanBerdasarkan,
                        @RequestParam(defaultValue = "asc") String arah) {
                Page<Penerbangan> data = adminService.ambilDataPenerbangan(page, size, urutanBerdasarkan, arah);

                return ResponseEntity.ok(ResponseApi.sukses("Data Berhasil DiTampilkan", data, HttpStatus.OK.value()));
        }

}
