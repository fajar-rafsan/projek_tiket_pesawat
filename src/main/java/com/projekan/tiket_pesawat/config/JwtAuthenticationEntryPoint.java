// package com.projekan.tiket_pesawat.config;

// import java.io.IOException;
// import java.util.HashMap;
// import java.util.Map;

// import org.springframework.http.HttpStatus;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.web.AuthenticationEntryPoint;
// import org.springframework.stereotype.Component;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.projekan.tiket_pesawat.dto.ResponseApi;

// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// @Component
// public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

//     @Override
//     public void commence(HttpServletRequest request, HttpServletResponse response,
//             AuthenticationException authException) throws IOException, ServletException {
//         response.setContentType("application/json");
//         response.setStatus(HttpStatus.UNAUTHORIZED.value());
//         // String pesan = (String) request.getAttribute("pesan_untuk_jwt_yang _error");

//         // Map<String, Object> bodyResponseNya = new HashMap<>();
//         // bodyResponseNya.put("pesan", pesan != null ? pesan : "request tidak jelas");

//         // response.getWriter().write(new ObjectMapper()
//         //         .writeValueAsString(new ResponseApi<Map<String, Object>>(HttpStatus.UNAUTHORIZED.value(),
//         //                 "Info ada kesalahan", bodyResponseNya)));
//     }
// }
