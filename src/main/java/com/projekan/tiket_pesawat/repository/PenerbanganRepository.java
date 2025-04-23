package com.projekan.tiket_pesawat.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projekan.tiket_pesawat.models.Penerbangan;

@Repository
public interface PenerbanganRepository extends JpaRepository<Penerbangan, Long> {
        @Query("SELECT p FROM Penerbangan p "
                        + "WHERE p.ketersediaanPenerbangan = 'TERSEDIA'"
                        + "AND (:dariKota IS NULL OR LOWER(p.kotaKeberangkatan) LIKE LOWER(CONCAT('%', :dariKota, '%')))"
                        + "AND (:keKota IS NULL OR LOWER(p.kotaTujuan) LIKE LOWER(CONCAT('%', :keKota, '%')))"
                        + "AND (:tanggal IS NULL OR DATE(p.waktuKeberangkatan) = :tanggal)")
        List<Penerbangan> findFiltered(@Param("dariKota") String dariKota, @Param("keKota") String keKota,
                        @Param("tanggal") LocalDate tanggal);
}
