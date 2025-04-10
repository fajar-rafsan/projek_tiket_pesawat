package com.projekan.tiket_pesawat.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projekan.tiket_pesawat.dto.PenerbanganDto;
import com.projekan.tiket_pesawat.dto.ResponseApi;
import com.projekan.tiket_pesawat.models.Penerbangan;
import com.projekan.tiket_pesawat.repository.PenerbanganRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final PenerbanganRepository penerbanganRepository;

    @PostMapping("tambah-penerbangan")
    public ResponseEntity<ResponseApi<?>> tambahPenerbangan(@RequestBody @Valid PenerbanganDto request) {

        if (request.getKotaKeberangkatan().equalsIgnoreCase(request.getKotaTujuan())) {
            String errorNya = "Kota keberangkatan dan tujuan tidak boleh di tempat yang sama";
            return ResponseEntity.badRequest()
                    .body(ResponseApi.gagal("info ada kesalahan", errorNya, HttpStatus.NOT_FOUND.value()));
        }

        if (!request.getWaktuKedatangan().isAfter(request.getWaktuKeberangkatan())) {
            String errorNya = "Waktu tiba harus setelah waktu keberangkatan";
            return ResponseEntity.badRequest()
                    .body(ResponseApi.gagal("info ada kesalahan", errorNya, HttpStatus.NOT_FOUND.value()));

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

}
