package com.projekan.tiket_pesawat.services;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.projekan.tiket_pesawat.exception.EmailException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username}")
    private String asalEmail;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender javaMailSender;
    private final TemplateEngine engine;

    @Override
    public void kirimToken(String email, String verifikasiToken, String refreshToken) {
        try {
            Context context = new Context();
            context.setVariables(Map.of("email", email, "token", verifikasiToken, "refreshToken", refreshToken));
            String htmlKonten = engine.process("html/template_email", context);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setPriority(1);
            helper.setSubject("Kode Akses Anda – Jangan Berikan ke Siapa Pun");
            helper.setFrom(asalEmail);
            helper.setTo(email);
            helper.setText(htmlKonten, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Gagal mengirim email ke {}: {}", email, e.getMessage());
            throw new EmailException("Gagal mengirim email ke " + email + "Silahkan coba lagi.");
        } catch (Exception e) {
            logger.error("Kesalahan tak terduga saat megirim Email ke {}: {}", email, e);
            throw new EmailException("Kesalahan internal saat mengirim email");
        }
    }

    @Override
    public void kirimOtp(String email, String kodeOtp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            Context context = new Context();
            context.setVariable("otp", kodeOtp);
            context.setVariable("waktuKode", 5);
            context.setVariable("waktu_sekarang",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm")));

            String htmlKonten = engine.process("html/template_kirim_otp", context);
            helper.setPriority(1);
            helper.setTo(email);
            helper.setFrom(asalEmail);
            helper.setSubject("Kode OTP - Untuk lupa password");
            helper.setText(htmlKonten, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Gagal mengirim email ke {}: {}", email, e.getMessage());
            throw new EmailException("Gagal mengirim email ke " + email + "Silahkan coba lagi.");
        } catch (Exception e) {
            logger.error("Kesalahan tak terduga saat megirim Email ke {}: {}", email, e);
            throw new EmailException("Kesalahan internal saat mengirim email");
        }
    }
}
