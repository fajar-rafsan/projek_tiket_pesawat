package com.projekan.tiket_pesawat.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view")
public class ViewController {
    @GetMapping("/login")
    public String loginPage(){
        return "user/login";
    }
    @GetMapping("/register")
    public String registerPage(){
        return "user/register";
    }
}
