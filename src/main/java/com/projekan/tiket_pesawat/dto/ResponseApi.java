package com.projekan.tiket_pesawat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ResponseApi<T> {
    private int status;
    private String message;
    private T data;

    public ResponseApi(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    public ResponseApi(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
