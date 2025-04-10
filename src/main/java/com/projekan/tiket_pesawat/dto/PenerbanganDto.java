package com.projekan.tiket_pesawat.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PenerbanganDto {
    @NotBlank(message = "Nama maskapai tidak boleh kosong, Wajib di isi")
    private String maskapai;
    @NotBlank(message = "Kota Keberangkatan tidak boleh kosong, Wajib di isi")
    private String kotaKeberangkatan;
    @NotBlank(message = "Kota Tujuan tidak boleh kosong, Wajib di isi")
    private String kotaTujuan;
    @Future(message = "Waktu keberangkatan harus di masa depan")
    private LocalDateTime waktuKeberangkatan;
    @Future(message = "Waktu tiba harus di masa depan")
    private LocalDateTime waktuKedatangan;
    @DecimalMin(value = "0.0", inclusive = false, message = "Harga harus lebih besar dari 0 dan tidak boleh negatif")
    private BigDecimal hargaTiket;
    @Min(value = 1, message = "Jumlah Kursi harus minimal 1")
    private Integer kursi;
}
