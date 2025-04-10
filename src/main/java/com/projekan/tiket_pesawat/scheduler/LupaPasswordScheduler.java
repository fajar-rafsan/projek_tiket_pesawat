package com.projekan.tiket_pesawat.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.projekan.tiket_pesawat.models.OTP;
import com.projekan.tiket_pesawat.models.StatusOtp;
import com.projekan.tiket_pesawat.repository.OtpRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LupaPasswordScheduler {
    private final OtpRepository otpRepository;

    @Scheduled(fixedRate = 60000)
    public void matikanOtpYangSudahExpired() {
        List<OTP> otps = otpRepository.findAll();
        for (OTP otp : otps) {
            if (otp.getStatus() == StatusOtp.AKTIF &&
                    otp.getAwalBuatKode().isBefore(LocalDateTime.now())) {
                otp.setStatus(StatusOtp.EXPIRED);
                otpRepository.save(otp);
            }
        }
    }
}
