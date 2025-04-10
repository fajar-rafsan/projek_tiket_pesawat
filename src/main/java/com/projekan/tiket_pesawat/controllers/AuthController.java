package com.projekan.tiket_pesawat.controllers;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projekan.tiket_pesawat.utils.JwtUtil;
import com.projekan.tiket_pesawat.dto.RefreshTokenRequestDto;
import com.projekan.tiket_pesawat.dto.RefreshTokenResponse;
import com.projekan.tiket_pesawat.dto.RequestLoginDto;
import com.projekan.tiket_pesawat.dto.RequestRegisUser;
import com.projekan.tiket_pesawat.dto.ResponseApi;
import com.projekan.tiket_pesawat.exception.AdminException;
import com.projekan.tiket_pesawat.exception.EmailTidakDitemukan;
import com.projekan.tiket_pesawat.models.Role;
import com.projekan.tiket_pesawat.models.StatusTokenResfresh;
import com.projekan.tiket_pesawat.models.TokenRefresh;
import com.projekan.tiket_pesawat.models.User;
import com.projekan.tiket_pesawat.repository.TokenRefreshRepository;
import com.projekan.tiket_pesawat.repository.UserRepository;
import com.projekan.tiket_pesawat.services.CustomUserDetailsService;
import com.projekan.tiket_pesawat.services.EmailService;
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
        private final TokenRefreshRepository tokenRefreshRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;
        private final JwtUtil jwtUtil;

        @PostMapping("/daftar")
        public ResponseEntity<?> daftar(@RequestBody @Valid RequestRegisUser user) {
                if (userRepository.existsByEmail(user.getEmail())) {
                        return ResponseEntity
                                        .status(HttpStatus.CONFLICT)
                                        .body(new ResponseApi<>(HttpStatus.CONFLICT.value(),
                                                        "Email Tersebut Sudah Di Gunakan!"));
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
                                .body(new ResponseApi<>(HttpStatus.CREATED.value(), "User Berhasil Di Daftarkan"));
        }

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody @Valid RequestLoginDto request) {
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new EmailTidakDitemukan("Email Tidak Ditemukan atau salah"));

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(new ResponseApi<>(HttpStatus.UNAUTHORIZED.value(),
                                                        "password anda salah"));
                }

                String role = user.getRole().name();
                String token = jwtUtil.generateToken(user.getEmail(), role);
                TokenRefresh tokenRefresh = tokenRefreshService.buatTokenBaru(user.getEmail());
                emailService.kirimToken(user.getEmail(), token, tokenRefresh.getToken());
                Map<String, String> responseData = new HashMap<>();
                responseData.put("role", role);

                ResponseApi<Map<String, String>> responseApi = new ResponseApi<>(HttpStatus.OK.value(),
                                "Token berhasil dikirim ke email, silahkan gunakan Token tersebut untuk Authorize",
                                responseData);
                return ResponseEntity.ok(responseApi);
        }

        @PostMapping("refresh-token")
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
                ResponseApi<RefreshTokenResponse> responseApi = new ResponseApi<>(
                                HttpStatus.OK.value(), "Token berhasil direfresh");
                return ResponseEntity.ok(responseApi);
        }

        @PostMapping("/logout")
        public ResponseEntity<ResponseApi<?>> logout(@RequestBody RefreshTokenRequestDto request) {
                String tokenRefresh = request.getRefreshToken();

                if (tokenRefresh == null || tokenRefresh.isEmpty()) {
                        return ResponseEntity.badRequest().body(new ResponseApi<>(HttpStatus.BAD_REQUEST.value(),
                                        "token tidak boleh kosong, tolong berikan kode TokenRefresh anda"));
                }

                tokenRefreshService.logout(tokenRefresh);
                return ResponseEntity.ok(new ResponseApi<>(HttpStatus.OK.value(),
                                "Anda Berhasil Logout TokenRefresh anda berhasil di nonaktifkan"));
        }
}
