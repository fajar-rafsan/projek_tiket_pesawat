package com.projekan.tiket_pesawat.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.projekan.tiket_pesawat.dto.PenerbanganUpdateDto;
import com.projekan.tiket_pesawat.models.Penerbangan;

public interface AdminService {
    Map<String, Object> updatePenerbangan(Penerbangan penerbangan, PenerbanganUpdateDto request, String userNya);
    ByteArrayInputStream eksportKeExcell(Penerbangan penerbangan) throws IOException;
    Page<Penerbangan> ambilDataPenerbangan(int page, int size, String urutBerdasarkan, String arah);
}
