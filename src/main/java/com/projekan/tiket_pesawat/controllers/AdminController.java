package com.projekan.tiket_pesawat.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projekan.tiket_pesawat.dto.PenerbanganDto;
import com.projekan.tiket_pesawat.models.Penerbangan;
import com.projekan.tiket_pesawat.repository.PenerbanganRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final PenerbanganRepository penerbanganRepository;

    @PostMapping("tambah-penerbangan")
    public Penerbangan tambahPenerbangan(@RequestBody PenerbanganDto request) {
        Penerbangan penerbanganDataBaru = Penerbangan.builder()
                .maskapai(request.getMaskapai())
                .kotaKeberangkatan(request.getKotaKeberangkatan())
                .kotaTujuan(request.getKotaTujuan())
                .waktuKeberangkatan(request.getWaktuKeberangkatan())
                .waktuKedatangan(request.getWaktuKedatangan())
                .hargaTiket(request.getHargaTiket())
                .kursi(request.getKursi()).build();

        return penerbanganRepository.save(penerbanganDataBaru);
    }
    
}
