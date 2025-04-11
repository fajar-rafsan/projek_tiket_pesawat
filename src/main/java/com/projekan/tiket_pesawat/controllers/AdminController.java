package com.projekan.tiket_pesawat.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projekan.tiket_pesawat.dto.PenerbanganDto;
import com.projekan.tiket_pesawat.dto.PenerbanganUpdateDto;
import com.projekan.tiket_pesawat.dto.ResponseApi;
import com.projekan.tiket_pesawat.exception.TidakDitemukanException;
import com.projekan.tiket_pesawat.models.Penerbangan;
import com.projekan.tiket_pesawat.repository.PenerbanganRepository;
import com.projekan.tiket_pesawat.services.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
                    .body(ResponseApi.gagal("info ada kesalahan", errorNya, HttpStatus.BAD_REQUEST.value()));
        }

        if (!request.getWaktuKedatangan().isAfter(request.getWaktuKeberangkatan())) {
            String errorNya = "Waktu tiba harus setelah waktu keberangkatan";
            return ResponseEntity.badRequest()
                    .body(ResponseApi.gagal("info ada kesalahan", errorNya, HttpStatus.BAD_REQUEST.value()));

        }

        Penerbangan penerbanganDataBaru = Penerbangan.builder()
                .maskapai(request.getMaskapai())
                .kotaKeberangkatan(request.getKotaKeberangkatan())
                .kotaTujuan(request.getKotaTujuan())
                .waktuKeberangkatan(request.getWaktuKeberangkatan())
                .waktuKedatangan(request.getWaktuKedatangan())
                .hargaTiket(request.getHargaTiket())
                .kursi(request.getKursi()).build();
        penerbanganRepository.save(penerbanganDataBaru);
        return ResponseEntity.ok(ResponseApi.sukses("Data Penerbangan Berhasil Di Tambahkan", penerbanganDataBaru,
                HttpStatus.OK.value()));
    }

    @PutMapping("/update-data-penerbangan/{id}")
    public ResponseEntity<ResponseApi<?>> updateDataPenerbangan(@PathVariable Long id,
            @RequestBody PenerbanganUpdateDto request) {
        Penerbangan penerbangan = penerbanganRepository.findById(id)
                .orElseThrow(() -> new TidakDitemukanException("Penerbangan dengan ID " + id + " Tidak ditemukan"));
        Map<String, Object> perubahan = adminService.updatePenerbangan(penerbangan, request);
        
        return ResponseEntity.ok(ResponseApi.sukses("Data Penerbangan Berhasil Di Update!", perubahan,
                HttpStatus.OK.value()));
    }
}
