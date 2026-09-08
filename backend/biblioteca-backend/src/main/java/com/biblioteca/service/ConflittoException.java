package com.biblioteca.service;

public class ConflittoException extends RuntimeException {
    public ConflittoException(String messaggio) {
        super(messaggio);
    }
}
