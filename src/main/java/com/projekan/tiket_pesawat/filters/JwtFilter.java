package com.projekan.tiket_pesawat.filters;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekan.tiket_pesawat.utils.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtutil;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs");
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            handleUnauthorized(response, "Token tidak ditemukan. Silakan login terlebih dahulu.");
            return;
        }

        String token = authHeader.substring(7);
        String email = null;
        String role = null;

        try {
            email = jwtutil.extractEmail(token);
            role = jwtutil.extractRole(token);
        } catch (ExpiredJwtException error) {
            handleUnauthorized(response, "Token sudah expired. Silakan login ulang.");
            throw new BadCredentialsException("JWT Expired", error);
        } catch (JwtException error) {
            handleUnauthorized(response, "Token tidak valid. Silakan periksa kembali token Anda.");
            request.setAttribute("pesan_untuk_jwt_yang _error",
                    "Token anda tidak Valid, Silahkan Periksa kembali Token anda");
            throw new BadCredentialsException("JWT Invalid", error);
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtutil.validateToken(token, userDetails.getUsername())) {
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleUnauthorized(HttpServletResponse response, String pesan) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> errorResponse = Map.of("status", HttpServletResponse.SC_UNAUTHORIZED,
                "pesan", pesan, "timestamp", LocalDateTime.now().toString());
        
        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
