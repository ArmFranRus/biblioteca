package com.biblioteca.service;

public class AccessNegatoException extends RuntimeException {
    public AccessNegatoException(String messaggio) {
        super(messaggio);
    }
}
