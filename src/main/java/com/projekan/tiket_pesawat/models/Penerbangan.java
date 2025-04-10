package com.projekan.tiket_pesawat.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Penerbangan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String maskapai;

    @Column(nullable = false, name = "kota_keberangkatan")
    private String kotaKeberangkatan;

    @Column(nullable = false, name = "kota_tujuan")
    private String kotaTujuan;

    @Column(nullable = false,name = "waktu_keberangkatan")
    private LocalDateTime waktuKeberangkatan;

    @Column(nullable = false, name = "waktu_kedatangan")
    private LocalDateTime waktuKedatangan;

    @Column(nullable = false, name = "harga_tiket")
    private BigDecimal hargaTiket;

    private Integer kursi;
}
