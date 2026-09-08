package com.biblioteca.service;

public class EmailGiaRegistrataException extends RuntimeException {
    public EmailGiaRegistrataException(String email) {
        super("Email gia' registrata: " + email);
    }
}
