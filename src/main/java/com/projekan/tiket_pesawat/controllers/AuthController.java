package com.projekan.tiket_pesawat.controllers;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projekan.tiket_pesawat.utils.JwtUtil;
import com.projekan.tiket_pesawat.dto.RefreshTokenRequestDto;
import com.projekan.tiket_pesawat.dto.RefreshTokenResponse;
import com.projekan.tiket_pesawat.dto.RequestLoginDto;
import com.projekan.tiket_pesawat.dto.RequestRegisUser;
import com.projekan.tiket_pesawat.dto.ResponseApi;
import com.projekan.tiket_pesawat.exception.AdminException;
import com.projekan.tiket_pesawat.exception.EmailTidakDitemukan;
import com.projekan.tiket_pesawat.models.OTP;
import com.projekan.tiket_pesawat.models.Role;
import com.projekan.tiket_pesawat.models.StatusOtp;
import com.projekan.tiket_pesawat.models.StatusTokenResfresh;
import com.projekan.tiket_pesawat.models.TokenRefresh;
import com.projekan.tiket_pesawat.models.User;
import com.projekan.tiket_pesawat.repository.OtpRepository;
import com.projekan.tiket_pesawat.repository.TokenRefreshRepository;
import com.projekan.tiket_pesawat.repository.UserRepository;
import com.projekan.tiket_pesawat.services.CustomUserDetailsService;
import com.projekan.tiket_pesawat.services.EmailService;
import com.projekan.tiket_pesawat.services.OtpService;
import com.projekan.tiket_pesawat.services.TokenRefreshService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
        private final UserRepository userRepository;
        private final CustomUserDetailsService customUserDetailsService;
        private final TokenRefreshService tokenRefreshService;
        private final OtpService otpService;
        private final OtpRepository otpRepository;
        private final TokenRefreshRepository tokenRefreshRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;
        private final JwtUtil jwtUtil;

        @PostMapping("/daftar")
        public ResponseEntity<?> daftar(@RequestBody @Valid RequestRegisUser user) {
                if (userRepository.existsByEmail(user.getEmail())) {
                        String pesanError = "Email Tersebut Sudah ada yang menggunakan";
                        return ResponseEntity
                                        .status(HttpStatus.CONFLICT)
                                        .body(ResponseApi.gagal("Info ada kesalahan", pesanError,
                                                        HttpStatus.CONFLICT.value()));
                }

                if (user.getRole() == Role.ADMIN && userRepository.existsByRole(user.getRole())) {
                        throw new AdminException("Tidak bisa Mendaftarkan akun sebagai Admin!");
                }

                User userBaru = User.builder()
                                .email(user.getEmail())
                                .password(passwordEncoder.encode(user.getPassword()))
                                .role(Role.USER)
                                .build();

                userRepository.save(userBaru);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ResponseApi.sukses("Sip!, Data anda sudah berhasil Daftar", userBaru,
                                                HttpStatus.CREATED.value()));
        }

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody @Valid RequestLoginDto request) {
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new EmailTidakDitemukan("Email Tidak Ditemukan atau salah"));

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        String errorNya = "Password anda salah!";
                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(ResponseApi.gagal("Info ada kesalahan!", errorNya,
                                                        HttpStatus.UNAUTHORIZED.value()));
                }

                String role = user.getRole().name();
                String token = jwtUtil.generateToken(user.getEmail(), role);
                TokenRefresh tokenRefresh = tokenRefreshService.buatTokenBaru(user.getEmail());
                emailService.kirimToken(user.getEmail(), token, tokenRefresh.getToken());
                Map<String, String> responseData = Map.of("PesanSukses",
                                "Token berhasil dikirim ke email, silahkan gunakan Token tersebut untuk Authorize",
                                "Role", role);

                return ResponseEntity.ok(ResponseApi.sukses("Berhasil!",
                                responseData,
                                HttpStatus.OK.value()));
        }

        @PostMapping("/refresh-token")
        public ResponseEntity<ResponseApi<RefreshTokenResponse>> refreshToken(
                        @RequestBody RefreshTokenRequestDto request) {
                String tokenRefreshNya = request.getRefreshToken();

                TokenRefresh tokenRefresh = tokenRefreshRepository.findByToken(tokenRefreshNya)
                                .orElseThrow(() -> new RuntimeException(
                                                "Refresh token tidak di temukan atau token yang anda berikan tidak valid"));
                TokenRefresh tokenValid = tokenRefreshService.cekTokenMasihInvalid(tokenRefreshNya)
                                .orElseThrow(() -> new RuntimeException("Token refresh anda tidak valid"));

                if (tokenRefresh.isSudahDicabut() || tokenRefresh.getKadaluarsa().isBefore(Instant.now())) {
                        tokenRefresh.setStatus(StatusTokenResfresh.SUDAH_EXPIRED);
                        tokenRefreshRepository.save(tokenRefresh);
                }

                String email = tokenValid.getEmailPengguna();
                UserDetails user = customUserDetailsService.loadUserByUsername(email);

                String tokenAksesBaru = jwtUtil.generateToken(email,
                                user.getAuthorities().iterator().next().getAuthority());

                TokenRefresh tokenBaru = tokenRefreshService.buatTokenBaru(email);
                tokenRefreshService.cabutToken(tokenRefresh.getToken());

                emailService.kirimToken(email, tokenAksesBaru, tokenBaru.getToken());
                return ResponseEntity.ok(ResponseApi.sukses(
                                "token berhasil di refresh, silahkan ambil lagi Token akses di email", null,
                                HttpStatus.OK.value()));
        }

        @PostMapping("/logout")
        public ResponseEntity<ResponseApi<?>> logout(@RequestBody RefreshTokenRequestDto request) {
                String tokenRefresh = request.getRefreshToken();

                if (tokenRefresh == null || tokenRefresh.isEmpty()) {
                        return ResponseEntity.badRequest().body(ResponseApi.gagal(
                                        "token tidak boleh kosong, tolong berikan kode TokenRefresh anda", null,
                                        HttpStatus.BAD_REQUEST.value()));
                }

                tokenRefreshService.logout(tokenRefresh);
                return ResponseEntity.ok(ResponseApi.sukses(
                                "Anda Berhasil Logout TokenRefresh anda berhasil di nonaktifkan", null,
                                HttpStatus.OK.value()));
        }

        @PostMapping("/request-lupa-password")
        public ResponseEntity<ResponseApi<?>> requestLupaPassword(@RequestParam String email) {
                if (userRepository.findByEmail(email).isEmpty()) {
                        return ResponseEntity.badRequest().body(ResponseApi.gagal(
                                        "Email anda Belum Terdaftar", null, HttpStatus.BAD_REQUEST.value()));
                }
                otpService.kirimOtp(email);
                return ResponseEntity.ok(ResponseApi.sukses(
                                "Kode OTP sudah berhasil dikirim ke Email anda", null, HttpStatus.OK.value()));
        }

        @PostMapping("/verifikasi-otp")
        public ResponseEntity<ResponseApi<?>> verifikasiOtp(@RequestParam String email, @RequestParam String otp) {
                boolean valid = otpService.verifikasiOtp(email, otp);
                if (!valid) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseApi.gagal(
                                        "Invalid kode OTP anda atau Sudah expired", null,
                                        HttpStatus.UNAUTHORIZED.value()));
                }
                return ResponseEntity.ok(ResponseApi.sukses(
                                "Kode OTP anda sudah, sekarang sudah bisa reset password anda", null,
                                HttpStatus.OK.value()));
        }

        @PostMapping("/reset")
        public ResponseEntity<ResponseApi<?>> passwordBaru(@RequestParam String email,
                        @RequestParam String passwordBaru) {
                Optional<OTP> otpOptional = otpRepository.findTopByEmailAndStatusOrderByAwalBuatKodeDesc(email,
                                StatusOtp.DIVERIFIKASI);

                if (otpOptional.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ResponseApi.gagal(
                                                        "Reset Password Gagal, Kode OTP anda belum di verifikasi", null,
                                                        HttpStatus.FORBIDDEN.value()));
                }

                Optional<User> userOtp = userRepository.findByEmail(email);
                if (userOtp.isPresent()) {
                        User user = userOtp.get();
                        user.setPassword(passwordEncoder.encode(passwordBaru));

                        userRepository.save(user);
                        OTP otp = otpOptional.get();
                        otp.setStatus(StatusOtp.EXPIRED);
                        otpRepository.save(otp);
                        return ResponseEntity.ok(ResponseApi.sukses(
                                        "Password anda Sudah berhasil diganti dengan Password Baru", null,
                                        HttpStatus.OK.value()));
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseApi.gagal(
                                "Akun anda tidak ditemukan coba cek kembali", null, HttpStatus.NOT_FOUND.value()));

        }
}
