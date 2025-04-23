package com.projekan.tiket_pesawat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projekan.tiket_pesawat.models.Booking;
import com.projekan.tiket_pesawat.models.StatusPembayaran;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatusPembayaranAndBatasWaktuPembayaranBefore(StatusPembayaran statusPembayaran, LocalDateTime batasWaktu);
    Optional<Booking> findByKodeBooking(String kodeBooking);
}
