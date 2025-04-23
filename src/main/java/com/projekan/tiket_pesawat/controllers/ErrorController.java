package com.projekan.tiket_pesawat.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.projekan.tiket_pesawat.dto.ResponseApi;
import com.projekan.tiket_pesawat.exception.AdminException;
import com.projekan.tiket_pesawat.exception.EmailException;
import com.projekan.tiket_pesawat.exception.EmailTidakDitemukan;
import com.projekan.tiket_pesawat.exception.StatusTidakValidException;
import com.projekan.tiket_pesawat.exception.TidakDitemukanException;
import com.projekan.tiket_pesawat.exception.TokenTidakDitemukan;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class ErrorController {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ResponseApi<Map<String, String>>> validasiException(MethodArgumentNotValidException e) {
                Map<String, String> errorNya = new HashMap<>();
                e.getBindingResult().getAllErrors().forEach(error -> {
                        String namaField = ((FieldError) error).getField();
                        String message = error.getDefaultMessage();
                        errorNya.put(namaField, message);
                });

                ResponseApi<Map<String, String>> responseApi = ResponseApi.gagal(
                                "Info ada kesalahan, Validasi gagal",
                                errorNya, HttpStatus.BAD_REQUEST.value());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseApi);
        }

        @ExceptionHandler(EmailException.class)
        public ResponseEntity<ResponseApi<?>> handleEmailException(EmailException error) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ResponseApi.gagal("Info ada Kesalahan Pada Email", error.getMessage(),
                                                HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }

        @ExceptionHandler(AdminException.class)
        public ResponseEntity<ResponseApi<?>> handleAdminException(AdminException error) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ResponseApi.gagal("Info ada kesalahan!", error.getMessage(),
                                                HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }

        @ExceptionHandler(ExpiredJwtException.class)
        public ResponseEntity<ResponseApi<?>> handleExpiredToken(ExpiredJwtException error) {
                Map<String, String> response = Map.of("pesan-error",
                                "Tokennya sudah Expired!, silahkan refresh token anda",
                                "error-Nya",
                                error.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ResponseApi.gagal("Info ada kesalahan!", response,
                                                HttpStatus.UNAUTHORIZED.value()));
        }

        @ExceptionHandler(JwtException.class)
        public ResponseEntity<ResponseApi<?>> handleJwtException(JwtException error) {
                Map<String, String> response = Map.of("pesan-error",
                                "Token anda Tidak valid, silahkan di cek lebih lanjut",
                                "error-Nya",
                                error.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ResponseApi.gagal("Info ada kesalahan!", response,
                                                HttpStatus.UNAUTHORIZED.value()));
        }

        @ExceptionHandler(EmailTidakDitemukan.class)
        public ResponseEntity<ResponseApi<?>> handleEmailTidakDitemukan(EmailTidakDitemukan error) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ResponseApi.gagal("Info ada kesalahan!", error.getMessage(),
                                                HttpStatus.UNAUTHORIZED.value()));
        }

        @ExceptionHandler(TokenTidakDitemukan.class)
        public ResponseEntity<ResponseApi<?>> handleTokenTidakDitemukan(TokenTidakDitemukan error) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ResponseApi.gagal("Info ada kesalahan!", error.getMessage(),
                                                HttpStatus.UNAUTHORIZED.value()));
        }

        @ExceptionHandler(TidakDitemukanException.class)
        public ResponseEntity<ResponseApi<?>> handleTidakDitemukan(TidakDitemukanException error) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ResponseApi.gagal("Info ada kesalahan!", error.getMessage(),
                                                HttpStatus.NOT_FOUND.value()));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ResponseApi<?>> handleBadRequest(IllegalArgumentException error) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ResponseApi.gagal("Info ada kesalahan!", error.getMessage(),
                                                HttpStatus.BAD_REQUEST.value()));
        }
        @ExceptionHandler(StatusTidakValidException.class)
        public ResponseEntity<ResponseApi<?>> handleStatusTidakValid(StatusTidakValidException error) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ResponseApi.gagal("Info ada kesalahan!", error.getMessage(),
                                                HttpStatus.BAD_REQUEST.value()));
        }

        
}
