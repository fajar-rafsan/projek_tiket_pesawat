package com.projekan.tiket_pesawat.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.projekan.tiket_pesawat.models.Role;
import com.projekan.tiket_pesawat.models.User;
import com.projekan.tiket_pesawat.repository.UserRepository;

@Configuration
public class AdminUserConfig {
    @Bean
    public CommandLineRunner admin(UserRepository userRepository, PasswordEncoder passwordEncoder){
        return args ->{
            if (!userRepository.existsByEmail("fajar.rafsan01@gmail.com")) {
                User admin = User.builder()
                                .email("fajar.rafsan01@gmail.com")
                                .password(passwordEncoder.encode("Admin12345"))
                                .role(Role.ADMIN)
                                .build();
                userRepository.save(admin);
                System.out.println("Pengguna Admin Berhasil Dibuat!");
            }
        };
    }
}
