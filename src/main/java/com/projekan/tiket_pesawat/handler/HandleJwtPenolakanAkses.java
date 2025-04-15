package com.projekan.tiket_pesawat.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HandleJwtPenolakanAkses implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        Map<String, Object> bodyResponse = Map.of("status", HttpServletResponse.SC_FORBIDDEN, "pesan",
                "Akses Ditolak!. kamu tidak punya izin ke endpoint ini", "timestamp", LocalDateTime.now().toString());

        new ObjectMapper().writeValue(response.getOutputStream(), bodyResponse);
    }

}
