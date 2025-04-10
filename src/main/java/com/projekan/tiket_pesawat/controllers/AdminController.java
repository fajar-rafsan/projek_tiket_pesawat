package com.projekan.tiket_pesawat.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @GetMapping()
    public ResponseEntity<?> admin(){
        return ResponseEntity.ok("Halo, Admin!");
    }
}
