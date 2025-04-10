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

        ResponseApi<Map<String, String>> responseApi = new ResponseApi<>(
                HttpStatus.BAD_REQUEST.value(),
                "Info ada Kesalahan",
                errorNya);
        return new ResponseEntity<>(responseApi, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ResponseApi<?>> handleEmailException(EmailException error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseApi<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Info ada kesalahan",
                        error.getMessage()));
    }

    @ExceptionHandler(AdminException.class)
    public ResponseEntity<ResponseApi<?>> handleAdminException(AdminException error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseApi<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Info ada kesalahan",
                        error.getMessage()));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ResponseApi<?>> handleExpiredToken(ExpiredJwtException error) {
        Map<String, String> response = new HashMap<>();
        response.put("pesan", "Tokennya sudah Expired!, silahkan refresh token anda");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseApi<>(HttpStatus.UNAUTHORIZED.value(), "Info ada kesalahan", response));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ResponseApi<?>> handleJwtException(JwtException error) {
        Map<String, String> response = new HashMap<>();
        response.put("pesan", "Token anda Tidak valid, silahkan di cek lebih lanjut");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseApi<>(HttpStatus.UNAUTHORIZED.value(), "Info ada Kesalahan", response));
    }

    @ExceptionHandler(EmailTidakDitemukan.class)
    public ResponseEntity<ResponseApi<?>> handleEmailTidakDitemukan(EmailTidakDitemukan error) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseApi<>(HttpStatus.UNAUTHORIZED.value(), error.getMessage()));
    }

    @ExceptionHandler(TokenTidakDitemukan.class)
    public ResponseEntity<ResponseApi<?>> handleTokenTidakDitemukan(TokenTidakDitemukan error) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseApi<>(HttpStatus.UNAUTHORIZED.value(), error.getMessage()));
    }
}
