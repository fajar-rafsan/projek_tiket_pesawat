package com.projekan.tiket_pesawat.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.projekan.tiket_pesawat.dto.PenerbanganUpdateDto;
import com.projekan.tiket_pesawat.models.Penerbangan;
import com.projekan.tiket_pesawat.repository.PenerbanganRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final PenerbanganRepository penerbanganRepository;

    @Override
    public Map<String, Object> updatePenerbangan(Penerbangan penerbangan, PenerbanganUpdateDto request) {

        Penerbangan salinDataLama = salinDataPenerbangan(penerbangan);

        Map<String, Object> perubahan = new HashMap<>();

        if (request.getKotaKeberangkatan() != null && request.getKotaTujuan() != null &&
                request.getKotaKeberangkatan().equalsIgnoreCase(request.getKotaTujuan())) {
            throw new IllegalArgumentException("Kota keberangkatan dan tujuan tidak boleh di tempat yang sama");
        }

        if (request.getWaktuKeberangkatan() != null && request.getWaktuKedatangan() != null &&
                !request.getWaktuKedatangan().isAfter(request.getWaktuKeberangkatan())) {
            throw new IllegalArgumentException("Waktu tiba harus setelah waktu keberangkatan");
        }
        Map<String, String> dataBaru = new HashMap<>();
        Map<String, String> dataLama = new HashMap<>();

        dataBaru.put("id", String.valueOf(penerbangan.getId()));
        dataLama.put("id", String.valueOf(penerbangan.getId()));

        if (request.getMaskapai() != null) {
            dataBaru.put("maskapai", request.getMaskapai());
            dataLama.put("maskapai", salinDataLama.getMaskapai());
            penerbangan.setMaskapai(request.getMaskapai());
        }
        if (request.getKotaKeberangkatan() != null) {
            dataBaru.put("kotaKeberangkatan", request.getKotaKeberangkatan());
            dataLama.put("kotaKeberangkatan", salinDataLama.getKotaKeberangkatan());
            penerbangan.setKotaKeberangkatan(request.getKotaKeberangkatan());
        }
        if (request.getKotaTujuan() != null) {
            dataBaru.put("kotaTujuan", request.getKotaTujuan());
            dataLama.put("kotaTujuan", salinDataLama.getKotaTujuan());
            penerbangan.setKotaTujuan(request.getKotaTujuan());
        }
        if (request.getWaktuKeberangkatan() != null) {
            dataBaru.put("WaktuKeberangkatan", request.getWaktuKeberangkatan().toString());
            dataLama.put("WaktuKeberangkatan", salinDataLama.getWaktuKeberangkatan().toString());
            penerbangan.setWaktuKeberangkatan(request.getWaktuKeberangkatan());
        }
        if (request.getWaktuKedatangan() != null) {
            dataBaru.put("WaktuKedatangan", request.getWaktuKedatangan().toString());
            dataLama.put("WaktuKedatangan", salinDataLama.getWaktuKedatangan().toString());
            penerbangan.setWaktuKedatangan(request.getWaktuKedatangan());
        }
        if (request.getHargaTiket() != null) {
            dataBaru.put("hargaTiket", String.valueOf(request.getHargaTiket()));
            dataLama.put("hargaTiket", String.valueOf(salinDataLama.getHargaTiket()));
            penerbangan.setHargaTiket(request.getHargaTiket());
        }
        if (request.getKursi() != null) {
            dataBaru.put("kursi", String.valueOf(request.getKursi()));
            dataLama.put("kursi", String.valueOf(salinDataLama.getKursi()));
            penerbangan.setKursi(request.getKursi());
        }
        perubahan.put("dataBaru", dataBaru);
        perubahan.put("dataLama", dataLama);
        penerbanganRepository.save(penerbangan);
        return perubahan;

    }

    private Penerbangan salinDataPenerbangan(Penerbangan p) {
        Penerbangan clone = new Penerbangan();
        clone.setId(p.getId());
        clone.setMaskapai(p.getMaskapai());
        clone.setKotaKeberangkatan(p.getKotaKeberangkatan());
        clone.setKotaTujuan(p.getKotaTujuan());
        clone.setWaktuKeberangkatan(p.getWaktuKeberangkatan());
        clone.setWaktuKedatangan(p.getWaktuKedatangan());
        clone.setHargaTiket(p.getHargaTiket());
        return clone;
    }
}
