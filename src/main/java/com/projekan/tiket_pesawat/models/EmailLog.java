package com.projekan.tiket_pesawat.models;

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
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String penerima;

    @Column(nullable = false)
    private String subjek;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(name = "waktu_pengiriman")
    private LocalDateTime waktuPengiriman;
}
