package com.projekan.tiket_pesawat.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RequestLoginDto {
    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Email Tidak Sesuai Format")
    private String email;
    @NotBlank(message = "Password tidak boleh kosong")
    private String password;
}
