package com.projekan.tiket_pesawat.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.projekan.tiket_pesawat.exception.TidakDitemukanException;

@ControllerAdvice
public class ErrorControllerHtml {
    
    @ExceptionHandler(TidakDitemukanException.class)
    public ModelAndView handleTidakDitemukan(TidakDitemukanException error){
        ModelAndView mav = new ModelAndView("html/error-nya");
        mav.addObject("pesan", error.getMessage());
        return mav;
    }
}
