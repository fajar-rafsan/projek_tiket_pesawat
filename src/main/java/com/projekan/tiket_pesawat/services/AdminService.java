package com.projekan.tiket_pesawat.services;

import java.util.Map;

import com.projekan.tiket_pesawat.dto.PenerbanganUpdateDto;
import com.projekan.tiket_pesawat.models.Penerbangan;

public interface AdminService {
    public Map<String, Object> updatePenerbangan(Penerbangan penerbangan, PenerbanganUpdateDto request);
}
