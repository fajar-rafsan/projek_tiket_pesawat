package com.projekan.tiket_pesawat.controllers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projekan.tiket_pesawat.exception.TidakDitemukanException;
import com.projekan.tiket_pesawat.models.Booking;
import com.projekan.tiket_pesawat.models.StatusPembayaran;
import com.projekan.tiket_pesawat.repository.BookingRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class HalamanWeb {

    private final BookingRepository bookingRepository;

    @GetMapping("/{idBooking}/halaman-verifikasi-pembayaran")
    public String tampilanHalamanVerifikasi(@PathVariable String kodeBooking, Model model) {
        try {
            Booking booking = bookingRepository.findByKodeBooking(kodeBooking).orElseThrow(
                    () -> new TidakDitemukanException("Booking dengan ID : " + kodeBooking + " Tidak di Temukan"));

            model.addAttribute("booking", booking);
            return "html/verifikasi_pembayaran";
        } catch (TidakDitemukanException error) {
            model.addAttribute("pesan", error);
            return "html/error-nya";
        }
    }

    @PostMapping("/{idBooking}/konfirmasi-pembayaran")
    public String konfirmasiPembayaran(@PathVariable String kodeBooking, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findByKodeBooking(kodeBooking).orElseThrow(
                () -> new TidakDitemukanException("Booking dengan ID : " + kodeBooking + " Tidak di Temukan"));

        booking.setStatusPembayaran(StatusPembayaran.SUDAH_DIBAYAR);
        booking.setWaktuPembayaran(LocalDateTime.now());
        bookingRepository.save(booking);

        redirectAttributes.addAttribute("sukses", true);
        return "redirect:/user/" + kodeBooking + "/halaman-verifikasi-pembayaran";
    }
}
