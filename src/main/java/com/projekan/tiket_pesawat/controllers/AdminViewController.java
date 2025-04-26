package com.projekan.tiket_pesawat.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/viewAdmin")
public class AdminViewController {
  
    @GetMapping("/dashboard")
    public String dashboard(){
        return "/admin/voler-main/voler-main/dist/index";
    }
    @GetMapping("/tambah-penerbangan")
    public String halamanTambahPenerbangan (){
       return "/admin/voler-main/voler-main/dist/tambah-penerbangan" ;
    }

    @GetMapping("/update-penerbangan")
    public String updatePenerbangan(){
       return "/admin/voler-main/voler-main/dist/update-penerbangan" ;
    }
    @GetMapping("/data-penerbangan")
    public String dataPenerbangan(){
        return "/admin/voler-main/voler-main/dist/data-penerbangan";
    }
    @GetMapping("/data-pemesanan")
    public String dataPemesanan(){
        return "/admin/voler-main/voler-main/dist/history-pemesanan";
    }

    @GetMapping("/update-history-penerbangan")
    public String updateHistoryPenerbangan(){
        return "/admin/voler-main/voler-main/dist/update-history-penerbangan";
    }

    @GetMapping("/status-pemesanan")
    public String statusPemesanan(){
        return "/admin/voler-main/voler-main/dist/status-pemesanan";
    }
    
}
