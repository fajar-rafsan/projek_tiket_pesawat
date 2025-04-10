package com.projekan.tiket_pesawat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projekan.tiket_pesawat.models.OTP;
import com.projekan.tiket_pesawat.models.StatusOtp;

public interface OtpRepository extends JpaRepository<OTP, Long>{
    Optional<OTP> findTopByEmailAndStatusOrderByAwalBuatKodeDesc(String email,StatusOtp status);
    Optional<OTP> findByEmailAndKodeOtp(String email, String kodeOtp);
}
