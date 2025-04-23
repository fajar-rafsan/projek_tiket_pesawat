package com.projekan.tiket_pesawat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projekan.tiket_pesawat.models.Kursi;

@Repository
public interface KursiRepository extends JpaRepository<Kursi, Long> {
    List<Kursi> findByPenerbanganId(Long PenerbanganId);
}
