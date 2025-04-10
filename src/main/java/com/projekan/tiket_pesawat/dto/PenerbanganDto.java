package com.projekan.tiket_pesawat.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PenerbanganDto {
    private String maskapai;
    private String kotaKeberangkatan;
    private String kotaTujuan;
    private LocalDateTime waktuKeberangkatan;
    private LocalDateTime waktuKedatangan;
    private BigDecimal hargaTiket;
    private Integer kursi;
}
