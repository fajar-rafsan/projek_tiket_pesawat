package com.projekan.tiket_pesawat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projekan.tiket_pesawat.models.Penerbangan;
import com.projekan.tiket_pesawat.models.UpdatePenerbanganHistory;

public interface UpdatePenerbanganHistoryRepository extends JpaRepository<UpdatePenerbanganHistory, Long>{
    List<UpdatePenerbanganHistory> findByPenerbangan(Penerbangan penerbangan);
}
